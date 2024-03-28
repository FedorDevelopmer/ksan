package com.ksan.lab4;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;

public class Controller {

    @FXML
    private Scene auth;
    @FXML
    private Stage stage;
    @FXML
    private Button send;
    @FXML
    private TextArea in;
    @FXML
    private TextArea out;
    @FXML
    private Button open;
    @FXML
    private Label fileOpened;
    @FXML
    private TextField request;
    @FXML
    private TextField serverIp;
    @FXML
    private TextField port;
    @FXML
    private TextField http;
    @FXML
    private Alert al;
    @FXML
    private ChoiceBox<String> method;
    @FXML
    private TextField pathFrom;
    @FXML
    private TextField pathTo;
    private File dir;
    private String JWT = "default";

    private String httpMethod="";

    public void initialize() {
        method.getItems().addAll("GET", "PUT", "POST","DELETE","MOVE","COPY");
        method.setOnAction(event->{
            String meth = method.getSelectionModel().getSelectedItem();
            if(meth==null){
                meth="GET";
            }
            httpMethod=meth;
            pathTo.setEditable(meth.equals("MOVE") || meth.equals("COPY"));
        });
    }
    public void OpenFile(){
        FileChooser fc = new FileChooser();
        fc.setTitle("Open file:");
        fc.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("All files","*.*"));
        dir = fc.showOpenDialog(stage);
        fileOpened.setText(dir.getPath());
    }

    public void Request(){
        String requestResult;
        Requester rq = new Requester(http.getText()+serverIp.getText()+":"+port.getText());
        rq.setJWT(JWT);
        String pathFromStr = pathFrom.getText();
        String pathToStr = pathTo.getText();
        Path filePath = Path.of(pathFromStr);
        Path filePathTo;
        if(!pathToStr.isEmpty()) {
            filePathTo = Path.of(pathToStr);
        }
        String fileType = pathFromStr.substring(pathFromStr.lastIndexOf(".")+1);
        switch (httpMethod.toLowerCase()) {
            case "get":
                requestResult = rq.GetRequest(filePath);
                if(fileType.contains("txt")) {
                    byte[] recData=requestResult.getBytes();
                    recData=Base64.getDecoder().decode(recData);
                    out.setText(new String(recData));
                }
                else{
                    FileChooser fchoose = new FileChooser();
                    fchoose.setTitle("Save File:");
                    fchoose.setInitialFileName("received."+fileType);
                    fchoose.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("File extension","."+fileType));
                    File saving = fchoose.showSaveDialog(stage);
                    FileOutputStream fs=null;
                    if(saving!=null) {
                        try {
                            fs = new FileOutputStream(saving);
                            byte[] recData=requestResult.getBytes();
                            recData=Base64.getDecoder().decode(recData);
                            fs.write(recData);
                            fs.close();
                        }
                        catch (Exception e){
                            System.out.println("ERROR: "+e.getMessage());
                        }finally{
                            if(fs!=null){
                                fs=null;
                            }
                            System.gc();
                        }
                    }
                }
            break;
            case "put":
                if(fileType.equals("txt")) {
                    requestResult = rq.PutRequest(filePath, in.getText());
                    out.setText(requestResult);
                }
                else{
                    if(dir!=null){
                        requestResult = rq.PutFRequest(filePath, Path.of(dir.toURI()));
                        out.setText(requestResult);
                    }
                    else{
                        out.setText("Error: no file to load chosen!");
                    }
                }
                break;
            case "post":
                requestResult=rq.PostRequest(filePath,in.getText());
                out.setText(requestResult);
                break;
            case "delete":
                requestResult=rq.DeleteRequest(filePath);
                out.setText(requestResult);
                break;
            case "move":
                filePathTo = Path.of(pathTo.getText());
                requestResult=rq.MoveRequest(filePath,filePathTo);
                out.setText(requestResult);
                break;
            case "copy":
                filePathTo = Path.of(pathTo.getText());
                requestResult=rq.CopyRequest(filePath,filePathTo);
                out.setText(requestResult);
                break;
        }
        if(rq.getResponseCode()==401){
            try {
                List<String> userData = new LinkedList<>();
                userData.add(http.getText());
                userData.add(serverIp.getText());
                userData.add(port.getText());
                userData.add(String.valueOf(rq.getAuthPort()));
                FXMLLoader fxmlLoader = new FXMLLoader(FileStorage.class.getResource("auth.fxml"));
                Scene auth = new Scene(fxmlLoader.load(), 323, 281);
                Stage alert = new Stage();
                AuthController cont = fxmlLoader.getController();
                cont.setStage(alert);
                cont.setData(userData);
                alert.setScene(auth);
                alert.showAndWait();
                JWT = (String) alert.getUserData();
            }catch (IOException e){
                System.err.println("Error:"+e.getMessage());
            }
        }
        rq.disconnectURL();
    }
}