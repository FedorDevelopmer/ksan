package com.ksan.lab3;

import javafx.fxml.FXML;
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

public class Controller {
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
    private Alert al;

    private File dir;


    public void OpenFile(){
        FileChooser fc = new FileChooser();
        fc.setTitle("Open file:");
        fc.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("All files","*.*"));
        dir = fc.showOpenDialog(stage);
        fileOpened.setText(dir.getPath());
    }

    public void Request(){
        String requestResult;
        Requester rq = new Requester(serverIp.getText());
        String command = request.getText();
        command=command.trim();
        int sp = command.indexOf(" ");
        int tb = command.indexOf("\t");
        String type = command.substring(0, Math.max(sp, tb));
        command=command.substring(Math.max(sp, tb)+1);
        Path filePath = Path.of(command);
        String fileType = command.substring(command.lastIndexOf(".")+1);
        switch (type.toLowerCase()) {
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
                command=command.trim();
                Path filePathTo = Path.of(command.substring(command.lastIndexOf(" ")).trim());
                command=command.substring(0,command.lastIndexOf(" ")+1);
                filePath=Path.of(command.trim());
                requestResult=rq.MoveRequest(filePath,filePathTo);
                out.setText(requestResult);
                break;
            case "copy":
                command=command.trim();
                filePathTo = Path.of(command.substring(command.lastIndexOf(" ")).trim());
                command=command.substring(0,command.lastIndexOf(" ")+1);
                filePath=Path.of(command.trim());
                requestResult=rq.CopyRequest(filePath,filePathTo);
                out.setText(requestResult);
                break;
        }


    }
}