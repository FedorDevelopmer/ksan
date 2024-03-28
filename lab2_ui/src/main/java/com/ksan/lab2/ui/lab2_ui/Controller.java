package com.ksan.lab2.ui.lab2_ui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.io.IOException;

public class Controller {
    @FXML
    public ListView chat_field;
    private static boolean connected=false;
    @FXML
    private Stage stage;
    @FXML
    private String name="Unknown";
    @FXML
    private static byte[] ip={127,0,0,1};
    @FXML
    private static int port=5005;
    @FXML
    private Label port_field;
    @FXML
    private Label name_field;
    @FXML
    private Label ip_field;

    @FXML
    private Label status;
    @FXML
    private Alert al;
    @FXML
    private Button send;
    @FXML
    private TextField message;

    private static Client client=null;

    public int setPort(String port){
        port=port.trim();
        try{
            int port_num=Integer.parseInt(port);
            if(port_num>=0&&port_num<=65535){
                this.port=port_num;
                return port_num;
            }
            else{
                al = new Alert(Alert.AlertType.ERROR);
                al.setTitle("Ошибка");
                al.setHeaderText("Ошибка ввода!");
                al.setContentText("Некорректный формат ввода номера порта!");
                al.show();
                return 0;
            }

        }
        catch(NumberFormatException e){
            al = new Alert(Alert.AlertType.ERROR);
            al.setTitle("Ошибка");
            al.setHeaderText("Ошибка ввода!");
            al.setContentText("Некорректный формат ввода номера порта!");
            al.show();
            return 0;
        }

    }
    public void setName(String name){
        this.name=name;
    }
    public byte[] setIP(String ip) throws NumberFormatException{
        ip=ip.trim();
        char[] proc_ip=ip.toCharArray();
        byte[] address = new byte[4];
        byte curr_part=0;
        try{
            for(int i=0,j=0;i<proc_ip.length;i++){
                if(proc_ip[i]!='.'){
                    curr_part*=10;
                    curr_part = (byte) (curr_part + Integer.parseInt(Character.toString(proc_ip[i])));
                }
                else{
                    address[j]=curr_part;
                    curr_part=0;
                    j++;
                }

            }
            address[3]=curr_part;
        }
        catch (NumberFormatException e){
            al = new Alert(Alert.AlertType.ERROR);
            al.setTitle("Ошибка");
            al.setHeaderText("Ошибка ввода!");
            al.setContentText("Некорректный формат ввода IP!");
            al.show();
            this.ip=address;
            return address;
        }
        this.ip=address;
        return address;

    }

    @FXML
    public static void finish(){
        if(Controller.client!=null&&connected){
            try{
                client.send_message("/help");
                Client.getSocket().shutdownInput();
                Client.getSocket().shutdownOutput();
                Client.getInput().close();
                Client.getOutput().close();
                Client.getSocket().close();
            }
            catch(IOException e){
                System.out.println(e.getMessage());
            }
        }
    }
    public static String BytesToString(byte[] arr){
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<arr.length;i++){
            sb.append(Byte.toUnsignedInt(arr[i]));
            sb.append(".");
        }
        sb.delete(sb.length()-1,sb.length());
        return sb.toString();
    }

    @FXML
    public void Help(){
        al = new Alert(Alert.AlertType.INFORMATION);
        al.setTitle("Доступные команды:");
        al.setHeaderText("Команды:");
        al.setContentText("/name - изменение имени \n"+"/ip - изменение IP-адреса сервера(только для клиента) \n"
        +"/port - изменение порта сервера \n"+"/connect - подключение к серверу с заданными параметрами \n"+"/disconnect - отключение от сервера \n");
        al.show();
    }
    @FXML
    public void MessageSent() {
        String Message = message.getText().toLowerCase().trim();
        if (Message.equalsIgnoreCase("/help")) {
            Help();
        } else {
            if (Message.contains("/name ")) {
                Message = message.getText().trim();
                Message = Message.substring(5);
                Message = Message.trim();
                this.setName(Message);
                name_field.setText("Ваше имя: " + Message);
                message.setText("");
            } else {
                if (Message.contains("/ip ")) {
                    Message = message.getText().trim();
                    Message = Message.substring(3);
                    Message = Message.trim();
                    byte[] address = this.setIP(Message);
                    ip_field.setText("IP-адрес: " + BytesToString(address));
                    message.setText("");
                } else {
                    if (Message.contains("/port ")) {
                        Message = message.getText().trim();
                        Message = Message.substring(5);
                        Message = Message.trim();
                        int port_n = setPort(Message);
                        port_field.setText("Порт: " + port_n);
                        message.setText("");
                    } else {
                        if(Message.contains("/connect")){
                            if(client==null) {
                                client = new Client(this.name, BytesToString(this.ip), Integer.toString(this.port), chat_field);
                                client.connect();
                                if(client.isConnected()){
                                    status.setText("подключен");
                                    status.setTextFill(Color.GREENYELLOW);
                                }
                                else{
                                    status.setText("не подключен");
                                    status.setTextFill(Color.RED);
                                }

                            }
                            else{
                                if(client.isConnected()) {
                                    client.disconnect();
                                    client = null;
                                    chat_field.getItems().clear();
                                }
                                client = new Client(this.name,BytesToString(this.ip),Integer.toString(this.port),chat_field);
                                client.connect();
                                if(client.isConnected()){
                                    status.setText("подключен");
                                    status.setTextFill(Color.GREENYELLOW);
                                }
                                else{
                                    status.setText("не подключен");
                                    status.setTextFill(Color.RED);
                                }
                            }
                            message.setText("");
                        }else{
                            if(Message.contains("/disconnect")&&client!=null&&client.isConnected()){
                                client.disconnect();
                                client=null;
                                chat_field.getItems().clear();
                                status.setText("не подключен");
                                status.setTextFill(Color.RED);
                                message.setText("");
                            }else{
                                if(client!=null&&client.isConnected()&&message.getText().length()>0) {
                                    client.send_message(message.getText());
                                    chat_field.setItems(FXCollections.observableArrayList(client.getData()));
                                    message.setText("");
                                }
                                else{
                                    al=new Alert(Alert.AlertType.ERROR);
                                    al.setTitle("Error");
                                    al.setContentText("Message cannot be sent: no connection!");
                                    al.show();
                                }
                            }
                        }

                    }
                }
            }
        }

    }
}