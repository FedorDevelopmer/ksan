package com.ksan.lab4;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.MessageDigest;

import java.util.Base64;
import java.util.List;
import java.util.Map;

public class AuthController {

    @FXML
    private Stage stage;
    @FXML
    private Label head;
    @FXML
    private TextField name;
    @FXML
    private PasswordField pass;
    @FXML
    private Button send;
    @FXML
    private Label response;

    private List<String> userData;

    public void setData(List<String> data){
        this.userData=data;
    }

    public void SendData()  {
        URL url = null;
        HttpURLConnection connection = null;
        String username = name.getText();
        String password = pass.getText();
        if(!username.isEmpty()&&!password.isEmpty()&&username.length()>6&&username.length()<30&&password.length()>8&&password.length()<50){
            byte[] passBytes = password.getBytes();
            byte[] salt = SaltGeneration(username);
            byte[] passHash = new byte[passBytes.length+salt.length];
            byte[] result = new byte[1];
            //byte array of pass + salt filling
            for(int i=0;i<passBytes.length+salt.length;i++){
                if(i<passBytes.length){
                    passHash[i]=passBytes[i];
                }
                else{
                    passHash[i]=salt[i-passBytes.length];
                }
            }
            try {
                MessageDigest msgDgst = MessageDigest.getInstance("SHA-512");
                result = msgDgst.digest(passHash);
            }catch (NoSuchAlgorithmException e){
                System.err.println(e.getMessage());
            }
            String usernameBase64 = new String(Base64.getEncoder().encode(username.getBytes()));
            String passwordHashBase64 = new String(Base64.getEncoder().encode(passHash));
            String params = "?username="+usernameBase64+"&pass="+passwordHashBase64;
            try {
                 url = new URL(userData.get(0)+userData.get(1)+":"+userData.get(3)+params);
            }
            catch(MalformedURLException e){
                System.out.println("Incorrect url: "+e.getMessage());
            }
            try {
                 connection = (HttpURLConnection) url.openConnection();
                 connection.setRequestMethod("GET");
                 connection.setDoOutput(false);
                 connection.connect();
                 int responseCode = connection.getResponseCode();
                BufferedReader in = null;
                 if(responseCode==HttpURLConnection.HTTP_OK){
                     in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                 }
                 else{
                     in = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                 }
                StringBuilder responseBuilder = new StringBuilder();
                String inputLine;
                String resp;
                while ((inputLine = in.readLine()) != null) {
                    responseBuilder.append(inputLine);
                }
                in.close();
                resp = responseBuilder.toString();
                byte[] respBytes = resp.getBytes();
                respBytes = Base64.getDecoder().decode(respBytes);
                resp = new String(respBytes);
                response.setText(resp);
                if(response.getText().contains("Successfully authorized")){
                    Map<String,List<String>> h = connection.getHeaderFields();
                    stage.setUserData(h.get("Authorization-token").get(0));
                    stage.hide();
                }
                else{
                    stage.setUserData("default");
                }
            }
            catch (IOException e){
                e.printStackTrace();
            }

        }
    }

    public byte[] SaltGeneration(String text){
        byte[] saltArr = Base64.getEncoder().encode(text.getBytes());
        for(byte bt:saltArr){
            bt^=0xFFFF;
            bt++;
        }
        return saltArr;
    }

    public void setStage(Stage stage){
        this.stage=stage;
    }

}
