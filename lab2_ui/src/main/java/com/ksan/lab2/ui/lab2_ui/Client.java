package com.ksan.lab2.ui.lab2_ui;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.ListView;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class Client {

    private final  String name;
    private final String ip;
    private final String port;

    private final ListView chat;

    private boolean isConnected=false;
    private final List<String> data = new ArrayList<>();
    protected static Socket clsc = null;

    private ServerListen sl;

    private static DataInputStream input = null;
    private static DataOutputStream output = null;
    private int mess_count=0;

    Client(String name,String ip,String port,ListView chat){
        this.name=name;
        this.ip=ip;
        this.port=port;
        this.chat=chat;
    }

    public List<String> getData(){
        return data;
    }

    public boolean isConnected(){
        return isConnected;
    }

    public void connect(){
        try {
            clsc = new Socket(this.ip, Integer.parseInt(this.port));
            input = new DataInputStream(clsc.getInputStream());
            output = new DataOutputStream(clsc.getOutputStream());
            output.writeInt(this.mess_count);
            output.flush();
            int update_size = input.readInt();
            for(int i=0;i<update_size;i++){
                data.add(input.readUTF());
            }
            mess_count = data.size();
            sl = new ServerListen(clsc,input,output,data,chat);
            isConnected=true;
        }
        catch(IOException e){
            System.out.println(e.getMessage());
            isConnected=false;
        }
    }
    public void disconnect(){
        try{
            input.close();
            output.close();
            clsc.close();
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }
    public void send_message(String message){
        try{
            if(message.equals("/help")){
                output.writeUTF(message);
            }
            else {
                output.writeUTF(this.name + ": " + message);
            }
            output.flush();
        }
        catch(IOException e){
            System.out.println(e.getMessage());
        }
    }
    public static DataInputStream getInput(){
        return input;
    }
    public static DataOutputStream getOutput(){
        return output;
    }
    public static Socket getSocket(){
        return clsc;
    }
}
class ServerListen extends Thread{
    private Socket CliSer;

    private ListView chat;

    private List<String> data;
    private DataInputStream in;
    private DataOutputStream out;
    ServerListen(Socket sc, DataInputStream in, DataOutputStream out, List<String> data,ListView chat){
        this.CliSer=sc;
        this.in=in;
        this.out=out;
        this.data=data;
        this.chat=chat;
        this.start();
    }
    public void run(){
        while(!Client.clsc.isClosed()&&chat!=null){
            try{
                if(in.available()>0){
                    String message = in.readUTF();
                    data.add(message);
                    Platform.runLater(()->{
                    chat.setItems(FXCollections.observableList(data));
                    chat.scrollTo(data.size()-1);
                    });
                }
                Thread.sleep(100);
            }
            catch(Exception e){
                System.out.println(e.getMessage());
            }

        }

    }
}
