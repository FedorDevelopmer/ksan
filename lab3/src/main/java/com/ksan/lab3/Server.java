package com.ksan.lab3;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

public class Server {

    private static String IP_ADDRESS = "localhost";
    private static String dir = "C://WebStorage//";
    private static int status = 200;
    private static byte[] response;

    static {
        try {
            IP_ADDRESS = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        boolean ok = false;
        while (!ok){
            try {
                System.out.println("Enter port number");
                Scanner in = new Scanner(System.in);
                HttpServer server = HttpServer.create(new InetSocketAddress(in.nextInt()), 5);
                ok = true;
                System.out.println(IP_ADDRESS + ":" + server.getAddress().getPort());
                server.createContext("/", new MyHandler());
                server.setExecutor(null); // creates a default executor
                server.start();
            }catch (InputMismatchException e){
                System.out.println("Wrong input");
            }catch (BindException e){
                System.out.println("Port is already in use");
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private static class MyHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            response = new byte[0];
            String requestURI = exchange.getRequestURI().toString();
            System.out.println(exchange.getRequestMethod() + " " + exchange.getRequestURI() + " " + exchange.getProtocol());
            String filePath=GetFileName(requestURI);
            String filePathTo=GetTargetFileName(requestURI);
            try {
                switch (exchange.getRequestMethod()) {
                    //чтение,пермещение или копирование файла
                    case "GET":
                        String Method = GetTrueMethod(requestURI);
                        if(Method!=null&&Method.equalsIgnoreCase("MOVE")){
                            doMove(filePath, filePathTo);
                        }else if(Method!=null&&Method.equalsIgnoreCase("COPY")) {
                            doCopy(filePath, filePathTo);
                        }else {
                            doGet(filePath);
                        }
                        break;

                    //добавление в конец файла
                    case "POST":
                        doPost(exchange, filePath);
                        break;

                    //перезапись файла
                    case "PUT":
                        doPut(exchange, filePath);
                        break;

                    //удаление файла
                    case "DELETE":
                        doDelete(filePath);
                        break;
                    default:
                        status = 405;
                }
            }catch (FileNotFoundException | NoSuchFileException e) {
                System.out.println(e.getMessage());
                status = 404;
            } catch (IOException e){
                System.out.println(e.getMessage());
                status = 400;
            }
            exchange.sendResponseHeaders(status, response.length);
            OutputStream os = exchange.getResponseBody();
            os.write(response);
            os.close();
        }

        private static void doGet(String filePath) throws  IOException{
            if(Files.exists(Path.of(dir+ filePath))) {
                byte[] sendData = Files.readAllBytes(Paths.get(dir + filePath));
                response = Base64.getEncoder().encode(sendData);
            }
            else{
                response=Base64.getEncoder().encode(("Error: file to get doesnt exist!").getBytes());
            }
        }

        private static void doPost(HttpExchange exchange, String filePath) throws IOException{
            Path path = Paths.get(dir + filePath);
            File d = new File(dir+GetFileDir(filePath));
            if (!Files.exists(path)){
                d.mkdirs();
                Files.createFile(path);
            }
            StringBuilder bodystr = new StringBuilder();
            BufferedReader in = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
            String line;
            while ((line = in.readLine()) != null) {
                bodystr.append(line);
            }
            FileOutputStream fos=null;
            try {
                fos = new FileOutputStream(dir+filePath,true);
                String data = bodystr.toString();
                byte[] writeData = data.getBytes();
                writeData=Base64.getDecoder().decode(writeData);
                fos.write(writeData);
                response = Base64.getEncoder().encode(("New data for file "+filePath+" was appended!").getBytes());

            }
            catch(IOException e){
                System.out.println(e.getMessage());
            }finally {
                if(fos!=null){
                    fos.close();
                    fos=null;
                }
                System.gc();
            }
        }

        private static void doPut(HttpExchange exchange, String filePath) throws IOException{
            Path path = Paths.get(dir + filePath);
            File d = new File(dir+GetFileDir(filePath));
            if (!Files.exists(path)){
                d.mkdirs();
                Files.createFile(path);
            }
            StringBuilder bodystr = new StringBuilder();
            BufferedReader in = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
            String line;
            while ((line = in.readLine()) != null) {
                bodystr.append(line);
            }
           FileOutputStream fw=null;
            try {
               fw = new FileOutputStream(dir + filePath);
               String data=bodystr.toString();
               byte[] binData = data.getBytes();
               binData= Base64.getDecoder().decode(binData);
               fw.write(binData);
               response = Base64.getEncoder().encode(("Data in file "+filePath+" was rewritten!").getBytes());
            }
            catch(IOException e){
                System.out.println(e.getMessage());
            }finally {
                if(fw!=null){
                    fw.close();
                    fw=null;
                }
                System.gc();
            }
        }

        private static void doDelete(String filePath) throws IOException{
            if(Files.exists(Paths.get(dir + filePath))) {
                Files.delete(Paths.get(dir + filePath));
                response = Base64.getEncoder().encode(("File " + filePath + " was successfully deleted.").getBytes());
            }
            else{
                response=Base64.getEncoder().encode(("Error: file for delete doesnt exist!").getBytes());
            }
        }

        private static void doMove(String filePath, String filePathTo) throws IOException{
            if(Files.exists(Paths.get(dir + filePath))) {
                File direct = new File(dir + GetFileDir(filePathTo));
                direct.mkdirs();
                Files.move(Path.of(dir+filePath),Path.of(dir+filePathTo),StandardCopyOption.REPLACE_EXISTING);
                response = Base64.getEncoder().encode(("File "+filePath+" was successfully moved to "+filePathTo+"!").getBytes());
            }
            else{
                response=Base64.getEncoder().encode(("Error: file for move doesnt exist!").getBytes());
            }
        }

        private static void doCopy(String filePath, String filePathTo) throws IOException{
            if(Files.exists(Paths.get(dir + filePath))) {
                File direct = new File(dir + GetFileDir(filePathTo));
                direct.mkdirs();
                Files.copy(Path.of(dir+filePath),Path.of(dir+filePathTo),StandardCopyOption.COPY_ATTRIBUTES);
                response = Base64.getEncoder().encode(("File "+filePath+" was successfully copied to "+filePathTo+"!").getBytes());
            }
            else{
                response=Base64.getEncoder().encode(("Error: file for copy doesnt exist!").getBytes());
            }
        }

        private static String GetFileName(String url){
            String result;
            if(url.contains("&")) {
                result = url.substring(url.indexOf("fl_pth=") + 7, url.indexOf('&'));
            }
            else{
                result = url.substring(url.indexOf("fl_pth=") + 7);
            }
            result=result.replace("*","\\");
            return result;
        }
        private static String GetFileDir(String file){
            String result;
            if(file!=null) {
                result=file.substring(0,file.lastIndexOf("\\")+1);
                return result;
            }
            return null;
        }
        private static String GetTargetFileName(String url){
            String result;
            if(url.contains("fl_to")){
                if(url.contains("&")) {
                    result = url.substring(url.indexOf("fl_to=") + 6,url.lastIndexOf("&"));
                }
                else{
                    result = url.substring(url.indexOf("fl_to=") + 6);
                }
                result=result.replace("*","\\");
                return result;
            }
            return null;

        }
        private static String GetTrueMethod(String url){
            String result;
            if(url.contains("true_meth=")){
                result=url.substring(url.indexOf("true_meth=")+10);
                return result;
            }
            return null;
        }

    }
}
