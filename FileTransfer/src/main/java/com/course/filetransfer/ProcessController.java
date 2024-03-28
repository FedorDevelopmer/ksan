package com.course.filetransfer;

import com.ibm.icu.text.Transliterator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPCmd;
import org.apache.commons.net.ftp.FTPSClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;

public class ProcessController {
    Stage stage;
    @FXML
    private ListView<String> processes;
    @FXML
    private Button close;
    @FXML
    private Label filePathLabel;
    @FXML
    private Label loadingProgressLabel;

    private HistoryItem connectData;

    private List<TreeItem<String>> localPaths;

    private List<TreeItem<String>> remotePaths;

    private HistoryItem remote;

    private String localDir;

    private String remoteDir;

    private String currProcessingFile;

    private OutputStream out=null;

    private InputStream in=null;

    private String lang="en";

    private FileInputStream fis=null;
    private FileOutputStream fos=null;

    private long currWroteBytes=0;

    private CompletableFuture<Void> loading;

    public void initialize(){
        processes.setCellFactory(param->new FileProcessCell());

    }
    public void Close(){
        stage.hide();
    }
    public void CancelUpload(){
        try {
            loading.cancel(true);
            Thread.sleep(500);
            FTPClient loadClient = FTP.createClient(connectData.getIp(),connectData.getPort(),connectData.getUser(),connectData.getPass());
            loadClient.sendCommand(FTPCmd.DELETE, currProcessingFile);
        }catch (Exception e){
            if(e instanceof CancellationException){
                System.out.println("Loading cancelled");
            }else{
                e.printStackTrace();
            }

        }
        if(lang.equals("en")){
            close.setText("Close");
        }else{
            close.setText("Закрыть");
        }
        close.setOnAction(event->{
            Close();
        });
    }
    public void CancelDownload(){
        try {
            loading.cancel(true);
            Thread.sleep(500);
            Files.delete(Path.of(currProcessingFile));
        }catch (Exception e){
            if(e instanceof CancellationException){
                System.out.println("Loading cancelled");
            }else{
                e.printStackTrace();
            }
        }
        if(lang.equals("en")){
            close.setText("Close");
        }else{
            close.setText("Закрыть");
        }
        close.setOnAction(event->{
            Close();
        });
    }
    public void setStage(Stage stage,boolean load){
        this.stage=stage;
        stage.setOnShown(windowEvent -> {
            if(load) {
                load();
                close.setOnAction(actionEvent -> {
                    CancelUpload();
                });
            }else{
                download();
                close.setOnAction(actionEvent -> {
                    CancelDownload();
                });
            }
        });
    }
    public void setConnectData(HistoryItem data){
        this.connectData=data;
    }
    public void setData(List<TreeItem<String>> localPaths,List<TreeItem<String>> remotePaths,HistoryItem remote,String localDir,String remoteDir,String lang){
        this.localPaths=localPaths;
        this.remotePaths=remotePaths;
        this.localDir=localDir;
        this.remoteDir=remoteDir;
        this.remote = remote;
        this.lang=lang;
        loadTranslationJSON();
    }
    public void loadFile(String path,TreeItem<String> localPath,int currI) {
        try {
            Platform.runLater(()->{
                currProcessingFile =path+"/"+localPath.getValue();
            });
            FTPClient loadClient = FTP.createClient(connectData.getIp(),connectData.getPort(),connectData.getUser(),connectData.getPass());
            fis = new FileInputStream(FileTreeWork.formFullFilePath(localPath));
            loadClient.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
            loadClient.enterLocalPassiveMode();
            loadClient.changeWorkingDirectory(path);
            //in case of file with russian symbols
            Transliterator transliterator = Transliterator.getInstance("Russian-Latin/BGN");
            String localPathEng = transliterator.transliterate(localPath.getValue());
            localPathEng=localPathEng.replaceAll("ʹ","");
            localPathEng=localPathEng.replaceAll("ё","");
            localPathEng=localPathEng.replaceAll("№","N");
            out = loadClient.storeFileStream(localPathEng);
            System.out.println("Output stream set on "+out.toString());
            System.out.println("File path: "+localPath);
            long size = Files.size(Path.of(FileTreeWork.formFullFilePath(localPath)));
            byte[] bytesIn = new byte[4096];
            int count = 0;
            int read = 0;
            if(out!=null) {
                int finalCount=0;
                while (out!=null&&((read = fis.read(bytesIn)) != -1)&&!loading.isCancelled()) {
                    out.write(bytesIn, 0, read);
                    out.flush();
                    count++;
                    finalCount = count;
                    final double progress = (finalCount * 4096.0) / size;
                    System.out.print(progress + "\r");
                    String data = (String) processes.getItems().get(currI);
                    String[] dataArr = data.split(":");
                    System.out.print(progress+"\r");
                    Platform.runLater(()->processes.getItems().set(currI, dataArr[0] + ":" + progress + ":" + progress*100 + "%"));
                }
                if(out!=null) {
                    out.close();
                }
                System.out.println("Transfer finished");
                loadClient.logout();
                loadClient.disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (fis != null) {
                fis = null;
            }
        }

    }
    public void loadDirectory(String path,TreeItem<String> localPath,int currI,long size) throws IOException {
        FTP.createDirectory(connectData, path);
        FileTreeWork.addChildren(localPath);
        List<TreeItem<String>> localFileSubFiles = localPath.getChildren();
        if(localFileSubFiles!=null){
            for (TreeItem<String> child:localFileSubFiles){
                String fullChildPath = FileTreeWork.formFullFilePath(child);
                File childFile = new File(fullChildPath);
                if(childFile.isDirectory()&&!loading.isCancelled()) {
                    String subDirPath=path+"/"+child.getValue();
                    if(FTP.createDirectory(connectData, subDirPath)) {
                        loadDirectory(subDirPath, child, currI,size);
                    }
                }else{
                    try {
                        FTPClient loadClient = FTP.createClient(connectData.getIp(),connectData.getPort(),connectData.getUser(),connectData.getPass());
                        fis = new FileInputStream(fullChildPath);
                        loadClient.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
                        loadClient.enterLocalPassiveMode();
                        loadClient.changeWorkingDirectory(path);
                        //in case of file with russian symbols
                        Transliterator transliterator = Transliterator.getInstance("Russian-Latin/BGN");
                        String childEng = transliterator.transliterate(child.getValue());
                        childEng=childEng.replaceAll("ʹ","");
                        childEng=childEng.replaceAll("ё","");
                        childEng=childEng.replaceAll("№","N");
                        loadClient.sendCommand("AUTH");
                        out = loadClient.storeFileStream(childEng);
                        final String childEngF = childEng;
                        Platform.runLater(()->{
                            currProcessingFile =path+"/"+childEngF;
                        });
                        System.out.println("Output stream set on "+out.toString());
                        System.out.println("File path: "+child);
                        byte[] bytesIn = new byte[4096];
                        int count = 0;
                        int read = 0;
                        long finalCount=0;
                        if(out!=null) {
                            while (out!=null&&((read = fis.read(bytesIn)) != -1)&&!loading.isCancelled()) {
                                out.write(bytesIn, 0, read);
                                out.flush();
                                count++;
                                finalCount = count;
                                final double progress = (currWroteBytes+(finalCount * 4096.0)) / size;
                                System.out.print(progress + "\r");
                                String data = processes.getItems().get(currI);
                                String[] dataArr = data.split(":");
                                System.out.print(progress+"\r");
                                Platform.runLater(()->processes.getItems().set(currI, dataArr[0] + ":" + progress + ":" + progress*100 + "%"));
                            }
                            if(out!=null) {
                                out.close();
                            }
                            System.out.println("Transfer finished");
                        }
                        currWroteBytes+=finalCount*4096;
                        loadClient.logout();
                        loadClient.disconnect();
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (fis != null) {
                            fis = null;
                        }
                    }
                    System.out.println("Uploaded");
                }
            }
        }
    }

    public void downloadFile(String path,TreeItem<String> remotePath,int currI){
        try {
            Platform.runLater(()->{
                currProcessingFile =path+"/"+remotePath.getValue();
            });
            FTPClient loadClient = FTP.createClient(connectData.getIp(),connectData.getPort(),connectData.getUser(),connectData.getPass());
            fos = new FileOutputStream(path+"/"+remotePath.getValue());
            loadClient.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
            loadClient.enterLocalPassiveMode();
            long size =0;
            int response = loadClient.sendCommand("size",FileTreeWork.formFullFilePathRemote(remotePath));
            if(response<500){
                String resp = loadClient.getReplyString();
                resp=resp.substring(resp.lastIndexOf(" ")+1);
                resp=resp.replaceAll("\n","");
                resp=resp.replaceAll("\r","");
                size=Long.parseLong(resp);
            }
            in = loadClient.retrieveFileStream(FileTreeWork.formFullFilePathRemote(remotePath));
            System.out.println("Input stream set on "+in.toString());
            System.out.println("File path: "+remotePath);
            byte[] bytesIn = new byte[4096];
            int count = 0;
            int read = 0;
            if(fos!=null) {
                int finalCount=0;
                while (fos!=null&&((read = in.read(bytesIn)) != -1)&&!loading.isCancelled()) {
                    fos.write(bytesIn, 0, read);
                    fos.flush();
                    count++;
                    finalCount = count;
                    final double progress = (finalCount * 4096.0) / size;
                    System.out.print(progress + "\r");
                    String data = processes.getItems().get(currI);
                    String[] dataArr = data.split(":");
                    System.out.print(progress+"\r");
                    Platform.runLater(()->processes.getItems().set(currI, dataArr[0] + ":" + progress + ":" + progress*100 + "%"));
                }
                if(fos!=null) {
                    fos.close();
                }
                System.out.println("Download finished");
                loadClient.logout();
                loadClient.disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (fos != null) {
                fos = null;
            }
        }
    }

    public void downloadDirectory(String path,TreeItem<String> remotePath,int currI,long size) throws IOException {
        if(!Files.exists(Path.of(path))){
            Files.createDirectory(Path.of(path));
        }
        FileTreeWork.addRemoteChildren(remotePath, connectData);
        List<TreeItem<String>> remoteFileSubFiles = remotePath.getChildren();
        if(remoteFileSubFiles!=null){
            for (TreeItem<String> child:remoteFileSubFiles){
                String fullChildPath = FileTreeWork.formFullFilePathRemote(child);
                if(FileTreeWork.isDirectoryFtp(fullChildPath,remote)&&!loading.isCancelled()) {
                    String subDirPath=path+"/"+child.getValue();
                    if(!Files.exists(Path.of(fullChildPath))) {
                        Files.createDirectory(Path.of(fullChildPath));
                    }
                    downloadDirectory(subDirPath, child, currI,size);
                }else {
                    try {
                        Platform.runLater(()->{
                            currProcessingFile =path + "/" +child.getValue();
                        });
                        FTPClient loadClient = FTP.createClient(connectData.getIp(),connectData.getPort(),connectData.getUser(),connectData.getPass());
                        fos = new FileOutputStream(path + "/" +child.getValue());
                        loadClient.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
                        loadClient.enterLocalPassiveMode();
                        in = loadClient.retrieveFileStream(FileTreeWork.formFullFilePathRemote(child));
                        System.out.println("Input stream set on " + in.toString());
                        System.out.println("File path: " + child);
                        byte[] bytesIn = new byte[4096];
                        int count = 0;
                        int read = 0;
                        if (fos != null) {
                            long finalCount = 0;
                            while (fos != null && ((read = in.read(bytesIn)) != -1)&&!loading.isCancelled()) {
                                fos.write(bytesIn, 0, read);
                                fos.flush();
                                count++;
                                finalCount = count;
                                final double progress = (currWroteBytes + (finalCount * 4096.0)) / size;
                                System.out.print(progress + "\r");
                                String data = processes.getItems().get(currI);
                                String[] dataArr = data.split(":");
                                System.out.print(progress + "\r");
                                Platform.runLater(() -> processes.getItems().set(currI, dataArr[0] + ":" + progress + ":" + progress * 100 + "%"));
                            }
                            currWroteBytes += finalCount * 4096;
                            if (fos != null) {
                                fos.close();
                            }
                            System.out.println("Download finished");
                            loadClient.logout();
                            loadClient.disconnect();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (fos != null) {
                            fos = null;
                        }
                    }
                    System.out.println("Downloaded");
                }
            }
        }
    }
    public void load(){
        currWroteBytes=0;
        for(int i=0;i<localPaths.size();i++){
            TreeItem<String> localPath = localPaths.get(i);
            processes.getItems().add(localPath.getValue()+":"+0+":"+0);
        }
        loading = CompletableFuture.runAsync(() -> {
            for(int i=0;i<localPaths.size();i++) {
                TreeItem<String> localPath = localPaths.get(i);
                TreeItem<String> remotePath = remotePaths.get(i);
                try {
                    if (!(new File(FileTreeWork.formFullFilePath(localPath)).isDirectory())) {
                        final int currI = i;
                        loadFile(FileTreeWork.formFullFilePathRemote(remotePath), localPath, currI);
                        currWroteBytes = 0;
                    } else {
                        final int currI = i;
                        loadDirectory(remotePath.getValue() + "/" + localPath.getValue(), localPath, currI, getDirectorySize(localPath));
                        currWroteBytes = 0;
                    }
                    FileTreeWork.addRemoteChildren(remotePath, connectData);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        CompletableFuture<Void> buttonUpdate = CompletableFuture.runAsync(() -> {
            loading.join();
            Platform.runLater(() -> {
                if(lang.equals("en")){
                    close.setText("Close");
                }else{
                    close.setText("Закрыть");
                }
                close.setOnAction(event -> {
                    Close();
                });
            });
        });
        loading.whenComplete((res, exception) -> {
            if(exception instanceof CancellationException){
                System.out.println("Loading process stop");
            }else {
                exception.printStackTrace();
            }
        });

    }
    public void download(){
        currWroteBytes=0;
        for(int i=0;i<localPaths.size();i++){
            TreeItem<String> remotePath = remotePaths.get(i);
            processes.getItems().add(remotePath.getValue()+":"+0+":"+0);
        }
        loading = CompletableFuture.runAsync(() -> {
            for (int i = 0; i < localPaths.size(); i++) {
                TreeItem<String> localPath = localPaths.get(i);
                TreeItem<String> remotePath = remotePaths.get(i);
                try {
                    if (!FileTreeWork.isDirectoryFtp(FileTreeWork.formFullFilePathRemote(remotePath),remote)) {
                        final int currI = i;
                        downloadFile(FileTreeWork.formFullFilePath(localPath), remotePath, currI);
                    } else {
                        final int currI = i;
                        downloadDirectory(FileTreeWork.formFullFilePath(localPath) +"/"+ remotePath.getValue(), remotePath, currI, getRemoteDirectorySize(remotePath));
                        currWroteBytes = 0;
                    }
                    FileTreeWork.addRemoteChildren(remotePath, connectData);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        CompletableFuture<Void> buttonUpdate = CompletableFuture.runAsync(() -> {
            loading.join();
            Platform.runLater(() -> {
                if(lang.equals("en")){
                    close.setText("Close");
                }else{
                    close.setText("Закрыть");
                }
                close.setOnAction(event -> {
                    Close();
                });
            });
        });
        loading.whenComplete((res, exception) -> {
            if (exception != null) {
                if(exception instanceof CancellationException){
                    System.out.println("Loading process stop");
                }else {
                    exception.printStackTrace();
                }
            }
        });
    }

    public long getDirectorySize(TreeItem<String> localPath){
        long result=0;
        FileTreeWork.addChildren(localPath);
        List<TreeItem<String>> localFileSubFiles = localPath.getChildren();
        if(localFileSubFiles!=null){
            for (TreeItem<String> child:localFileSubFiles){
                String fullChildPath = FileTreeWork.formFullFilePath(child);
                File childFile = new File(fullChildPath);
                if(childFile.isDirectory()) {
                    result+=getDirectorySize(child);
                }else{
                    result+=childFile.length();
                }
            }
        }
        return result;
    }
    public long getRemoteDirectorySize(TreeItem<String> remotePath){
        long result=0;
        FileTreeWork.addRemoteChildren(remotePath, connectData);
        List<TreeItem<String>> remoteFileSubDirectories = remotePath.getChildren();
        if(remoteFileSubDirectories!=null){
            for (TreeItem<String> child:remoteFileSubDirectories){
                String fullChildPath = FileTreeWork.formFullFilePathRemote(child);
                if(FileTreeWork.isDirectoryFtp(fullChildPath,remote)) {
                    result+=getRemoteDirectorySize(child);
                }else{
                    try{
                        FTPClient client = FTP.createClient(connectData.getIp(),connectData.getPort(),connectData.getUser(),connectData.getPass());
                        int code = client.sendCommand("size",fullChildPath);
                        if(code<500) {
                            String resp = client.getReplyString();
                            resp=resp.substring(resp.lastIndexOf(" ")+1);
                            resp=resp.replaceAll("\n","");
                            resp=resp.replaceAll("\r","");
                            result+=Long.parseLong(resp);
                        }
                    }
                    catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }
        }
        return result;
    }
    public void loadTranslationJSON() {
        JSONParser parser = new JSONParser();
        try {
            InputStream in = FileTransfer.class.getResourceAsStream("FTPSimpleLang.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            JSONArray data = (JSONArray) parser.parse(reader);
            for (Object obj : data) {
                if (obj instanceof JSONObject) {
                    JSONObject object = (JSONObject) obj;
                    switch ((String) object.get("graphicName")) {
                        case "#filePathLabel" -> {
                            filePathLabel.setText((String) object.get(lang));
                        }
                        case "#loadingProgressLabel" -> {
                            loadingProgressLabel.setText((String) object.get(lang));
                        }
                        case "#close" -> {
                            switch (close.getText()) {
                                case "Отмена":
                                case "Cancel":
                                    close.setText((String) object.get(lang + "Cancel"));
                                    break;
                                case "Закрыть":
                                case "Close":
                                    close.setText((String) object.get(lang + "Close"));
                                    break;
                                default:
                                    close.setText("Close");
                                    break;
                            }
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
