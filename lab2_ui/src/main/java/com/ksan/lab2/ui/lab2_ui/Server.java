package com.ksan.lab2.ui.lab2_ui;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Server {

    private static LinkedList<ClientHandler> clients_connections=new LinkedList<>();
    public static void main(String[] args) {
        int cores_count = Runtime.getRuntime().availableProcessors();
        Scanner scan = new Scanner(System.in);
        int port=0;
        while(port==0){
            System.out.println("Enter port number: ");
            port = scan.nextInt();
            if (!isPortAvailable(port)) {
                System.out.println("Error: port is disabled to use. Try another port");
                port=0;
            }
            else{
                System.out.println("Success: port is enabled to use. Starting server on port "+port);
            }
        }

        try(ServerSocket sc = new ServerSocket(port)){
            System.out.println("Server started.");
            InetAddress ip = InetAddress.getLocalHost();
            String hostname = ip.getHostName();
            System.out.println("Server name "+hostname+" with ip "+BytesToIP(ip.getAddress()));
            System.out.println("Server port: "+sc.getLocalPort());
            Path dir = Paths.get(".//chat.txt");
            if(!Files.exists(dir)){
                Files.createFile(Paths.get(".//chat.txt"));
            }
            while(!sc.isClosed()){
                if(clients_connections.size()<cores_count) {
                    Socket client = sc.accept();
                    ClientHandler ch = new ClientHandler(client, clients_connections);
                    clients_connections.add(ch);
                    ch.start();
                }
                try{
                    Thread.sleep(100);
                }
                catch(InterruptedException e){
                    System.out.println(e.getMessage());
                }

            }
            System.out.println("Server work is finished.");
        }
        catch(IOException e){
            System.out.println("Error in opening sever socket");
        }

    }
    public static boolean isPortAvailable(int port){
        try(ServerSocket test_port = new ServerSocket(port)){
            test_port.close();
            return true;
        }
        catch(IOException e){
            return false;
        }
    }
    public static String BytesToIP(byte[] arr){
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<arr.length;i++){
            sb.append(Byte.toUnsignedInt(arr[i]));
            sb.append(".");
        }
        sb.delete(sb.length()-1,sb.length());
        return sb.toString();
    }

}
class ClientHandler implements Runnable{
    private Thread thr;

    private Socket serv;

    private LinkedList<ClientHandler> clients;

    private DataInputStream input;
    private DataOutputStream output;

    ClientHandler(Socket sc,LinkedList<ClientHandler> clients){
        this.serv=sc;
        this.clients=clients;
    }

    public void sendMessage(String message){
        if(!serv.isOutputShutdown()) {
            try {
                output.writeUTF(message);
            } catch (IOException e) {
                try {
                    input.close();
                    output.close();
                    serv.close();
                    System.out.println("Client disconnected");
                }catch (IOException ex){
                    System.out.println(ex.getMessage());
                }
            }
        }

    }

    public void run() {
        try{
            Socket client = this.serv;
            Path dir = Paths.get("./chat.txt");
            List<String> data=Files.readAllLines(dir);
            System.out.println("New client connected");
            input = new DataInputStream(client.getInputStream());
            output = new DataOutputStream(client.getOutputStream());
            int user_mess_count=input.readInt();
            output.writeInt(data.size()-user_mess_count);
            output.flush();
            for(int i=user_mess_count;i<data.size();i++){
                output.writeUTF(data.get(i));
                output.flush();
            }
            //message sent
            while(!client.isOutputShutdown()) {
                if(input.available()>0){
                    String user_mess = input.readUTF();
                    System.out.println(user_mess);
                    if(!user_mess.equals("/help")) {
                        data = Files.readAllLines(dir);
                        data.add(user_mess);
                        Files.write(dir, data);
                        clients.removeIf(clh -> clh.serv.isOutputShutdown());
                        for (ClientHandler clh : clients) {
                            clh.sendMessage(user_mess);
                        }
                    }
                    else{
                        client.shutdownOutput();
                    }
                }
                try{
                    Thread.sleep(100);
                }
                catch(InterruptedException e){
                    input.close();
                    output.close();
                    serv.close();
                    System.out.println("Interrupted Ex: "+e.getMessage());
                }

            }
            input.close();
            output.close();
            serv.close();
            System.out.println("Client disconnected");
        }
        catch (IOException e){
            System.out.println("Error in work of socket thread: " + e.getMessage());
        }
    }
    public void start(){
        if(thr==null){
            thr = new Thread(this,"0");
            thr.start();
        }
    }
}
