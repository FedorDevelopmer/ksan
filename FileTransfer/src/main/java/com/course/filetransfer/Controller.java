package com.course.filetransfer;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.apache.commons.net.ftp.FTPClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class Controller {
    // files holder views
    @FXML
    private TreeView<String> yourFiles;
    @FXML
    private TreeView<String> remoteFiles;
    @FXML
    private ListView<ListCell<String>> uploadFiles;
    @FXML
    private ListView<ListCell<String>> downloadFiles;
    @FXML
    private Button connect;
    @FXML
    private TextField host;
    @FXML
    private TextField port;
    @FXML
    private TextField localDir;
    @FXML
    private TextField remoteDir;
    @FXML
    private TextField localDestination;
    @FXML
    private TextField remoteDestination;
    @FXML
    private TextField login;
    @FXML
    private PasswordField password;
    @FXML
    private Label hostLabel;
    @FXML
    private Label portLabel;
    @FXML
    private Label loginLabel;
    @FXML
    private Label passwordLabel;
    @FXML
    private Label yourFilesLabel;
    @FXML
    private Label remoteDeviceLabel;
    @FXML
    private Button remDestDir;
    @FXML
    private Button localDestDir;
    @FXML
    private Button localDirectory;
    @FXML
    private Button remoteDirectory;
    @FXML
    private ChoiceBox<HistoryItem> connectionHistory;
    @FXML
    private TextArea responses;
    @FXML
    private VBox mainContainer;
    @FXML
    private Button upload;
    @FXML
    private Button download;
    @FXML
    public ContextMenu optionsForFile=new ContextMenu();
    @FXML
    public ContextMenu optionsForRemoteFile=new ContextMenu();
    @FXML
    public ContextMenu removeFromQueueOption=new ContextMenu();
    @FXML
    public ContextMenu removeFromRemoteQueueOption=new ContextMenu();
    @FXML
    private Button updateLocal;
    @FXML
    private Button updateRemote;
    @FXML
    private Button clearUpload;
    @FXML
    private Button clearDownload;
    @FXML
    private MenuItem language;
    @FXML
    private MenuItem help;
    @FXML
    private MenuItem rus;
    @FXML
    private MenuItem eng;
    @FXML
    private MenuItem about;
    @FXML
    protected ProgressIndicator connectProgress;

    private CompletableFuture<Void> connection=null;

    private HistoryItem connectionData;

    private EventHandler<ConnectionEvent> connectionHandler;

    private FTP ftp;

    private HistoryItem chosenConf;

    protected Path historyPath = Path.of("history");

    protected Scene mainScene;

    private String lang = "ru";

    private Alert al = new Alert(Alert.AlertType.INFORMATION);

    public void initialize() {
        getFiles();
        DialogPane pane = al.getDialogPane();
        pane.setId("title");
        connectProgress.setVisible(false);
        connectProgress.setPrefSize(40,40);
        connectProgress.setMaxSize(40,40);
        updateLocal.setText("");
        updateRemote.setText("");
        ImageView graphLocal =new ImageView(Objects.requireNonNull(getClass().getResource("images/update.png")).toExternalForm());
        graphLocal.setFitWidth(updateLocal.getPrefWidth()-5);
        graphLocal.setFitHeight(updateLocal.getPrefHeight()-5);
        updateLocal.setGraphic(graphLocal);
        updateLocal.setPadding(new Insets(0));
        ImageView graphRemote =new ImageView(Objects.requireNonNull(getClass().getResource("images/update.png")).toExternalForm());
        graphRemote.setFitWidth(updateRemote.getPrefWidth()-5);
        graphRemote.setFitHeight(updateRemote.getPrefHeight()-5);
        updateRemote.setGraphic(graphRemote);
        updateRemote.setPadding(new Insets(0));
        connectionHandler = new EventHandler<ConnectionEvent>() {
            @Override
            public void handle(ConnectionEvent event) {
                if(event.getStatus(ConnectionStatus.CONNECTED)) {
                    optionsForFile.getItems().get(0).setDisable(false);
                    optionsForFile.getItems().get(1).setDisable(false);
                    updateRemote.setDisable(false);
                }else if(event.getStatus(ConnectionStatus.DISCONNECTED)){
                    optionsForFile.getItems().get(0).setDisable(true);
                    optionsForFile.getItems().get(1).setDisable(true);
                    updateRemote.setDisable(true);
                    remoteFiles.setRoot(null);
                }
            }
        };
        yourFiles.setContextMenu(optionsForFile);
        remoteFiles.setContextMenu(optionsForRemoteFile);
        uploadFiles.setContextMenu(removeFromQueueOption);
        downloadFiles.setContextMenu(removeFromRemoteQueueOption);
        yourFilesContextMenuHandlers();
        remoteFilesContextMenuHandlers();
        removeFromQueueHandler();
        removeFromRemoteQueueHandler();
        mainHandlers();
        menuItemHandlers();
        if(Files.exists(historyPath)){
            List<HistoryItem> historyElements = (List<HistoryItem>) Serializer.deserialize(historyPath);
            for(HistoryItem data : historyElements){
                if(!(connectionHistory.getItems().contains(data))) {
                    connectionHistory.getItems().add(data);
                }
            }
        }

    }
    public void connectionButton(){
        if(ftp !=null){
            if(connection!=null){
                connection.cancel(true);
                if(lang.equals("en")) {
                    connect.setText("Connect");
                }else{
                    connect.setText("Подключиться");
                }
            }
            FTPDisconnect();
        }else{
            if(lang.equals("en")) {
                connect.setText("Cancel");
            }else{
                connect.setText("Отмена");
            }
            FTPConnect();
        }
    }
    public void FTPConnect(){
        ftp = new FTP(this);
        connectProgress.setVisible(true);
        connection = CompletableFuture.runAsync(()->{
            FTPClient tryClient=null;
            try {
                tryClient = ftp.loginFTP(host.getText(), port.getText(), login.getText(), password.getText());
            }catch(Exception e){
                System.out.println("Connection error");
            }
            final FTPClient tryClientFinal = tryClient;
            if(tryClientFinal!=null&&tryClientFinal.isConnected()&&!connection.isCancelled()){
                Platform.runLater(()-> {
                    Event.fireEvent(this.optionsForFile, new ConnectionEvent(ConnectionStatus.CONNECTED));
                });
                    HistoryItem newUser = new HistoryItem(host.getText(), port.getText(), login.getText(), password.getText());
                Platform.runLater(()-> {
                    if (!connectionHistory.getItems().contains(newUser)) {
                        if (connectionHistory.getItems().size() < 10) {
                            connectionHistory.getItems().add(newUser);
                        } else {
                            ObservableList<HistoryItem> items = connectionHistory.getItems();
                            for (int i = items.size(); i > 0; i--) {
                                items.set(i, items.get(i - 1));
                            }
                            items.set(0, newUser);
                        }
                    }
                });
                    chosenConf=newUser;
                    TreeItem<String> remRoot = new TreeItem<>("/");
                    SetImage(remRoot, 20, 20);
                    FileTreeWork.addRemoteChildren(remRoot, chosenConf);
                    Platform.runLater(()->{
                        remoteDir.setText(remRoot.getValue());
                        remoteDestination.setText(remRoot.getValue());
                        remoteFiles.setRoot(remRoot);
                        if(lang.equals("en")) {
                            connect.setText("Disconnect");
                        }else{
                            connect.setText("Отключиться");
                        }

                    });

            }else{
                if(!connection.isCancelled()) {
                ftp = null;
                Platform.runLater(() -> {
                    Alert connectionError = new Alert(Alert.AlertType.ERROR);
                    connectionError.getDialogPane().setPrefHeight(200);
                    if(lang.equals("en")) {
                        connect.setText("Connect");
                        connectionError.setHeaderText("Error occurred!");
                        connectionError.setTitle("Connection error:");
                        connectionError.setContentText("Unable to connect server,connection process terminated by error,check input data and server availability");
                        connectionError.showAndWait();
                    }else{
                        connect.setText("Подключиться");
                        connectionError.setHeaderText("Возникла ошибка!");
                        connectionError.setTitle("Ошибка подключения:");
                        connectionError.setContentText("Невозможно подключиться, процесс подключения прерван ошибкой, проверьте данные для подключения и доступность сервера");
                        connectionError.showAndWait();
                    }
                });
                }
            }
        });
        connection.whenComplete((res,exception)->{
            connectProgress.setVisible(false);
        });
    }
    public void FTPDisconnect(){
        try {
            if(ftp!=null) {
                if(ftp.remote!=null) {
                    ftp.remote.logout();
                    ftp.remote.disconnect();
                }
                Event.fireEvent(this.optionsForFile, new ConnectionEvent(ConnectionStatus.DISCONNECTED));
                ftp = null;
            }
            remoteDir.setText("");
            remoteDestination.setText("");
            remoteFiles.setRoot(null);
        }catch (Exception e){
            e.printStackTrace();
            ftp.remote=null;
            ftp=null;
            Event.fireEvent(this.optionsForFile, new ConnectionEvent(ConnectionStatus.DISCONNECTED));
        }
    }

    public void getFiles() {
        File[] roots= File.listRoots();
        File rootDir = roots[0];
        TreeItem<String> root = new TreeItem<>(rootDir.toString());
        SetImage(root,20,20);
        root=FileTreeWork.addChildren(root);
        yourFiles.setRoot(root);
        localDir.setText(root.getValue());
        localDestination.setText(root.getValue());
    }

    public void FileChosen(){
        TreeItem<String> selected = yourFiles.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if(selected.getChildren()!=null&&selected.getChildren().size()==0) {
                selected=FileTreeWork.addChildren(selected);
            }
            localDir.setText(FileTreeWork.formFullFilePath(selected));
            localDestination.setText(localDir.getText());
        }
    }
    public void RemoteFileChosen(){
        TreeItem<String> selected = remoteFiles.getSelectionModel().getSelectedItem();
        if(selected!=null) {
            if (selected.getChildren() != null && selected.getChildren().size() == 0) {
                selected = FileTreeWork.addRemoteChildren(selected, chosenConf);
            }
            remoteDir.setText(FileTreeWork.formFullFilePathRemote(selected));
            remoteDestination.setText(remoteDir.getText());
        }
    }
    public void SetImage(TreeItem<String> root,int width,int height) {
        Image icon = new Image(getClass().getResource("images/dir.png").toExternalForm());
        ImageView view = new ImageView(icon);
        view.setFitWidth(width);
        view.setFitHeight(height);
        root.setGraphic(view);
    }
    public void yourFilesContextMenuHandlers(){
        MenuItem loadInstantly = new MenuItem("Upload to remote instantly");
        MenuItem addToQueue = new MenuItem("Add to upload queue");
        loadInstantly.setId("uploadInstantly");
        addToQueue.setId("addToUploadQueue");
        loadInstantly.setOnAction(event->{
            TreeItem<String> remotePath = remoteFiles.getSelectionModel().getSelectedItem();
            TreeItem<String> localPath = yourFiles.getSelectionModel().getSelectedItem();
            Stage proc = new Stage();
            if(FileTreeWork.isDirectoryFtp(FileTreeWork.formFullFilePathRemote(remotePath),chosenConf)) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("processWin.fxml"));
                    Scene procScene = new Scene(loader.load());
                    ProcessController cont = loader.getController();
                    cont.setStage(proc, true);
                    cont.setConnectData(chosenConf);
                    if (remotePath == null) {
                        remoteFiles.getSelectionModel().select(0);
                        remotePath = remoteFiles.getSelectionModel().getSelectedItem();
                    }
                    List<TreeItem<String>> localPaths = new ArrayList<>();
                    List<TreeItem<String>> remotePaths = new ArrayList<>();
                    String localPathString = localDir.getText();
                    String remotePathString = remoteDir.getText() + localPath.getValue();
                    localPaths.add(localPath);
                    remotePaths.add(remotePath);
                    cont.setData(localPaths, remotePaths, chosenConf, localPathString, remotePathString,lang);
                    proc.setScene(procScene);
                    proc.showAndWait();
                    updateRemote(remoteFiles.getRoot());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        addToQueue.setOnAction(event->{
            TreeItem<String> item = yourFiles.getSelectionModel().getSelectedItem();
            if(item!=null){
                FileTreeWork.addListElement(uploadFiles,item,connectionData);
                if(uploadFiles.getItems().size()>0){
                    upload.setDisable(false);
                    clearUpload.setDisable(false);
                }
            }
        });
        optionsForFile.addEventHandler(ConnectionEvent.EventType(),connectionHandler);
        optionsForFile.getItems().add(loadInstantly);
        optionsForFile.getItems().add(addToQueue);
        optionsForFile.getItems().get(0).setDisable(true);
        optionsForFile.getItems().get(1).setDisable(true);
        updateRemote.setDisable(true);
    }
    public void remoteFilesContextMenuHandlers(){
        MenuItem loadInstantly = new MenuItem("Download to remote instantly");
        MenuItem addToQueue = new MenuItem("Add to download queue");
        loadInstantly.setId("downloadInstantly");
        addToQueue.setId("addToDownloadQueue");
        loadInstantly.setOnAction(event->{
            TreeItem<String> remotePath = remoteFiles.getSelectionModel().getSelectedItem();
            TreeItem<String> localPath = yourFiles.getSelectionModel().getSelectedItem();
            Stage proc = new Stage();
            if(Files.isDirectory(Path.of(FileTreeWork.formFullFilePath(localPath)))) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("processWin.fxml"));
                    Scene procScene = new Scene(loader.load());
                    ProcessController cont = loader.getController();
                    cont.setStage(proc, false);
                    cont.setConnectData(chosenConf);
                    if (remotePath == null) {
                        remoteFiles.getSelectionModel().select(0);
                        remotePath = remoteFiles.getSelectionModel().getSelectedItem();
                    }
                    List<TreeItem<String>> localPaths = new ArrayList<>();
                    List<TreeItem<String>> remotePaths = new ArrayList<>();
                    String localPathString = localDir.getText();
                    String remotePathString = remoteDir.getText();
                    localPaths.add(localPath);
                    remotePaths.add(remotePath);
                    cont.setData(localPaths, remotePaths, chosenConf, localPathString, remotePathString,lang);
                    proc.setScene(procScene);
                    proc.showAndWait();
                    updateLocal(yourFiles.getRoot());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        addToQueue.setOnAction(event->{
            TreeItem<String> item = remoteFiles.getSelectionModel().getSelectedItem();
            if(item!=null){
                FileTreeWork.addListElementRemote(downloadFiles,item,connectionData);
                if(downloadFiles.getItems().size()>0){
                    download.setDisable(false);
                    clearDownload.setDisable(false);
                }
            }
        });
        optionsForRemoteFile.addEventHandler(ConnectionEvent.EventType(),connectionHandler);
        optionsForRemoteFile.getItems().add(loadInstantly);
        optionsForRemoteFile.getItems().add(addToQueue);
    }
    public void mainHandlers(){
        upload.setOnAction(event->{
            ObservableList items = uploadFiles.getItems();
            List<TreeItem<String>> localPaths = new ArrayList<>();
            List<TreeItem<String>> remotePaths = new ArrayList<>();
            Stage proc = new Stage();
            try{
                FXMLLoader loader = new FXMLLoader(getClass().getResource("processWin.fxml"));
                Scene procScene = new Scene(loader.load());
                ProcessController cont = loader.getController();
                cont.setStage(proc,true);
                cont.setConnectData(chosenConf);
                TreeItem<String> remotePath = remoteFiles.getSelectionModel().getSelectedItem();
                if(FileTreeWork.isDirectoryFtp(FileTreeWork.formFullFilePathRemote(remotePath),chosenConf)) {
                    String localPathString = localDestination.getText();
                    String remotePathString = remoteDestination.getText();
                    for (int i = 0; i < items.size(); i++) {
                        String fullPath = ((FileListCell) items.get(i)).getFullPath();
                        String item = FileTreeWork.getElementName(fullPath);
                        TreeItem<String> localPath = FileTreeWork.findTreeItemByNameAndPath(yourFiles.getRoot(), item, fullPath);
                        if (remotePath == null) {
                            remoteFiles.getSelectionModel().select(0);
                            remotePath = remoteFiles.getSelectionModel().getSelectedItem();
                        }
                        localPaths.add(localPath);
                        remotePaths.add(remotePath);
                    }
                    cont.setData(localPaths, remotePaths, chosenConf, localPathString, remotePathString,lang);
                    proc.setScene(procScene);
                    proc.showAndWait();
                    FileTreeWork.addRemoteChildren(remotePath, chosenConf);
                }
            }
            catch (IOException e){
                e.printStackTrace();
            }
        });
        download.setOnAction(event->{
            ObservableList items = downloadFiles.getItems();
            List<TreeItem<String>> localPaths = new ArrayList<>();
            List<TreeItem<String>> remotePaths = new ArrayList<>();
            Stage proc = new Stage();
            try{
                FXMLLoader loader = new FXMLLoader(getClass().getResource("processWin.fxml"));
                Scene procScene = new Scene(loader.load());
                ProcessController cont = loader.getController();
                cont.setStage(proc,false);
                cont.setConnectData(chosenConf);
                TreeItem<String> localPath = yourFiles.getSelectionModel().getSelectedItem();
                if(Files.isDirectory(Path.of(FileTreeWork.formFullFilePath(localPath)))) {
                    String localPathString = localDestination.getText();
                    String remotePathString = remoteDestination.getText();
                    for (int i = 0; i < items.size(); i++) {
                        String fullPath = ((FileListCell) items.get(i)).getFullPath();
                        String item = FileTreeWork.getElementName(fullPath);
                        TreeItem<String> remotePath = FileTreeWork.findTreeItemByNameAndPath(remoteFiles.getRoot(), item, fullPath);
                        if (localPath == null) {
                            yourFiles.getSelectionModel().select(0);
                            localPath = remoteFiles.getSelectionModel().getSelectedItem();
                        }
                        localPaths.add(localPath);
                        remotePaths.add(remotePath);
                    }
                    cont.setData(localPaths, remotePaths, chosenConf, localPathString, remotePathString,lang);
                    proc.setScene(procScene);
                    proc.showAndWait();
                    FileTreeWork.addChildren(localPath);
                }
            }
            catch (IOException e){
                e.printStackTrace();
            }
        });
        connectionHistory.setOnAction(event->{
            HistoryItem selected = connectionHistory.getSelectionModel().getSelectedItem();
            connectionData=selected;
            if(selected!=null){
                chosenConf=selected;
                connectionHistory.getSelectionModel().clearSelection();
                host.setText(selected.getIp());
                port.setText(selected.getPort());
                login.setText(selected.getUser());
                password.setText(selected.getPass());
            }
        });
        updateLocal.setOnAction(actionEvent -> {
            updateLocal(yourFiles.getRoot());
        });
        updateRemote.setOnAction(actionEvent -> {
            updateRemote(remoteFiles.getRoot());
        });
        clearUpload.setOnAction(actionEvent -> {
            uploadFiles.getItems().clear();
            upload.setDisable(true);
            clearUpload.setDisable(true);
        });
        clearDownload.setOnAction(actionEvent -> {
            downloadFiles.getItems().clear();
            download.setDisable(true);
            clearDownload.setDisable(true);
        });
    }
    public void removeFromQueueHandler(){
        MenuItem remove = new MenuItem("Remove");
        remove.setId("removeFromQueue");
        remove.setOnAction(actionEvent -> {
            ListCell<String> item = uploadFiles.getSelectionModel().getSelectedItem();
            if(item!=null) {
                uploadFiles.getItems().remove(item);
                if(uploadFiles.getItems().size()==0){
                    upload.setDisable(true);
                    clearUpload.setDisable(true);
                }
            }
        });
        removeFromQueueOption.getItems().add(remove);
    }
    public void removeFromRemoteQueueHandler(){
        MenuItem remove = new MenuItem("Remove");
        remove.setOnAction(actionEvent -> {
            ListCell<String> item = downloadFiles.getSelectionModel().getSelectedItem();
            if(item!=null) {
                downloadFiles.getItems().remove(item);
                if(downloadFiles.getItems().size()==0){
                    download.setDisable(true);
                    clearDownload.setDisable(true);
                }
            }
        });
        removeFromRemoteQueueOption.getItems().add(remove);
    }
    public void updateLocal(TreeItem<String> root){
        if(root!=null){
            if(root.getChildren().size()>0){
                for(TreeItem<String> child:root.getChildren()){
                    if(child.getChildren().size()>0){
                        updateLocal(child);
                    }
                }
                FileTreeWork.addChildren(root);
            }
        }
    }
    public void updateRemote(TreeItem<String> root){
        if(root!=null){
            if(root.getChildren().size()>0){
                for(TreeItem<String> child:root.getChildren()){
                    if(child.getChildren().size()>0){
                        updateRemote(child);
                    }
                }
                FileTreeWork.addRemoteChildren(root,chosenConf);
            }
        }
    }
    public void menuItemHandlers(){
        about.setOnAction(actionEvent -> {
            DialogPane pane = al.getDialogPane();
            pane.setPrefHeight(400);
            al.show();
        });
        rus.setOnAction(actionEvent -> {
            lang="ru";
            loadTranslationJSON();
        });
        eng.setOnAction(actionEvent -> {
            lang="en";
            loadTranslationJSON();
        });
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
                        case "#hostLabel" -> {
                            hostLabel.setText((String) object.get(lang));
                        }
                        case "#portLabel" -> {
                            portLabel.setText((String) object.get(lang));
                        }
                        case "#loginLabel" -> {
                            loginLabel.setText((String) object.get(lang));
                        }
                        case "#passwordLabel" -> {
                            passwordLabel.setText((String) object.get(lang));
                        }
                        case "#localDestDir" -> {
                            localDestDir.setText((String) object.get(lang));
                        }
                        case "#remDestDir" -> {
                            remDestDir.setText((String) object.get(lang));
                        }
                        case "#yourFilesLabel" -> {
                           yourFilesLabel.setText((String) object.get(lang));
                        }
                        case "#remoteDeviceLabel" -> {
                            remoteDeviceLabel.setText((String) object.get(lang));
                        }
                        case "#localDirectory" -> {
                           localDirectory.setText((String) object.get(lang));
                        }
                        case "#remoteDirectory" -> {
                            remoteDirectory.setText((String) object.get(lang));
                        }
                        case "#connect" -> {
                            switch (connect.getText()) {
                                case "Подключиться":
                                case "Connect":
                                    connect.setText((String) object.get(lang + "Connect"));
                                break;
                                case "Отключиться":
                                case "Disconnect":
                                    connect.setText((String) object.get(lang + "Disconnect"));
                                break;
                                case "Отмена":
                                case "Cancel":
                                    connect.setText((String) object.get(lang + "Cancel"));
                                break;
                                default:
                                    connect.setText("Connect");
                                break;
                            }
                        }
                        case "#upload" -> {
                            upload.setText((String) object.get(lang));
                        }
                        case "#clearUpload" -> {
                            clearUpload.setText((String) object.get(lang));
                        }
                        case "#download" -> {
                            download.setText((String) object.get("ru"));
                        }
                        case "#clearDownload" -> {
                            clearDownload.setText((String) object.get(lang));
                        }
                        case "#uploadInstantly" ->{
                            optionsForFile.getItems().get(0).setText((String) object.get(lang));
                        }
                        case "#addToUploadQueue" ->{
                            optionsForFile.getItems().get(1).setText((String) object.get(lang));
                        }
                        case "#downloadInstantly" ->{
                            optionsForRemoteFile.getItems().get(0).setText((String) object.get(lang));
                        }
                        case "#addToDownloadQueue" ->{
                            optionsForRemoteFile.getItems().get(1).setText((String) object.get(lang));
                        }
                        case "#removeFromQueue" ->{
                            removeFromQueueOption.getItems().get(0).setText((String) object.get(lang));
                        }
                        case "#removeFromRemoteQueue" ->{
                            removeFromRemoteQueueOption.getItems().get(0).setText((String) object.get(lang));
                        }
                        case "#language" ->{
                            language.setText((String) object.get(lang));
                        }
                        case "#help" ->{
                            help.setText((String) object.get(lang));
                        }
                        case "#rus" ->{
                            rus.setText((String) object.get(lang));
                        }
                        case "#eng" ->{
                            eng.setText((String) object.get(lang));
                        }
                        case "#about" ->{
                            about.setText((String) object.get(lang));
                        }
                        case "#title"->{
                            Alert alertControl = al;
                            Object alertData = object.get(lang);
                            if (alertData instanceof JSONObject) {
                                JSONObject alertDataJson = (JSONObject) alertData;
                                alertControl.setTitle((String) alertDataJson.get("title"));
                                alertControl.setHeaderText((String) alertDataJson.get("header"));
                                alertControl.setContentText((String) alertDataJson.get("par1"));
                                for (int i = 2; i < 6; i++) {
                                    alertControl.setContentText(alertControl.getContentText() + alertDataJson.get("par" + i));
                                }
                                alertControl.setContentText(alertControl.getContentText() + "\n");
                                alertControl.setContentText(alertControl.getContentText() + alertDataJson.get("credits"));
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