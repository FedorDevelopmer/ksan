package com.ksan.lab4;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.security.NoSuchAlgorithmException;
import java.security.MessageDigest;

public class Server {

    private static final String serverSecret = "52a8b05fdf8165d9544d0298428877fa9bf932f06b3837ac125d7759e3c60159840edfd359e3b84e5faa3eed9aa737bfa84c1312f37c8744143c1c6d315a1955";
    private static String IP_ADDRESS = "localhost";
    private static String dir = "C:/WebStorage/";

    private static String usersJsonDir = "C:/WebStorage/users.json";
    private static int status = 200;
    private static byte[] response;

    private static int authPort=0;

    private static String userFolder="";

    private static int serverPort=0;

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
                Scanner in = new Scanner(System.in);
                System.out.println("Enter port number for file server:");
                serverPort = in.nextInt();
                HttpServer fileServer = HttpServer.create(new InetSocketAddress(serverPort), 5);
                System.out.println("Enter port number for auth server:");
                authPort = in.nextInt();
                HttpServer authServer = HttpServer.create(new InetSocketAddress(authPort), 5);
                ok = true;
                System.out.println(IP_ADDRESS + ":" + fileServer.getAddress().getPort());
                fileServer.createContext("/", new FileHandler());
                fileServer.setExecutor(null);
                fileServer.start();
                authServer.createContext("/", new AuthHandler());
                authServer.setExecutor(null);
                authServer.start();
            }catch (InputMismatchException e){
                System.out.println("Wrong input");
            }catch (BindException e){
                System.out.println("Port is already in use");
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private static class FileHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            response = new byte[0];
            String requestURI = exchange.getRequestURI().toString();
            System.out.println(exchange.getRequestMethod() + " " + exchange.getRequestURI() + " " + exchange.getProtocol());
            String filePath=GetFileName(requestURI);
            String filePathTo=GetTargetFileName(requestURI);
            boolean authorized = AuthorizeUser(exchange);
                if(authorized) {
                   dir = "C:/WebStorage/"+userFolder+"/";
                try {
                    switch (exchange.getRequestMethod()) {
                        //чтение,пермещение или копирование файла
                        case "GET":
                            String Method = GetTrueMethod(requestURI);
                            if (Method != null && Method.equalsIgnoreCase("MOVE")) {
                                doMove(filePath, filePathTo);
                            } else if (Method != null && Method.equalsIgnoreCase("COPY")) {
                                doCopy(filePath, filePathTo);
                            } else {
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
                } catch (FileNotFoundException | NoSuchFileException e) {
                    System.out.println(e.getMessage());
                    status = 404;
                    response= Base64.getEncoder().encode(("Error 404:Not found").getBytes());
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                    status = 400;
                    response= Base64.getEncoder().encode(("Error 400:Bad Request").getBytes());
                }
            }
            else{
                status=401;
                List<String> str = new LinkedList<>();
                str.add(String.valueOf(authPort));
                exchange.getResponseHeaders().put("Auth-port",str);
                response= Base64.getEncoder().encode(("Error 401:Unauthorized").getBytes());
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
            result=new String(Base64.getUrlDecoder().decode(result.getBytes()));
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
                result=new String(Base64.getUrlDecoder().decode(result.getBytes()));
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

        private static boolean AuthorizeUser(HttpExchange exchange){
            Headers bearer = exchange.getRequestHeaders();
            String username = "";
            String currTime = "";
            String type = "";
            String algorithm = "";
            String issuer = "";
            String header = "";
            String payload = "";
            String signature = "";
            if(bearer.get("Authorization")!=null&&!bearer.get("Authorization").get(0).isEmpty()){
                String webToken = bearer.get("Authorization").get(0);
                if(webToken.contains(".")) {
                     header = webToken.substring(webToken.indexOf("Bearer ")+7, webToken.indexOf('.'));
                    webToken = webToken.substring(webToken.indexOf('.') + 1);
                     payload = webToken.substring(0, webToken.indexOf('.'));
                    webToken = webToken.substring(webToken.indexOf('.') + 1);
                     signature = webToken;
                } else{
                    header=new String(Base64.getUrlEncoder().encode(("none").getBytes()));
                    payload=header;
                    signature=header;
                }
                JSONParser parser = new JSONParser();
                try {
                    String headerEncoded = new String(Base64.getUrlDecoder().decode(header.getBytes()));
                    String payloadEncoded = new String(Base64.getUrlDecoder().decode(payload.getBytes()));
                    Object headerParsed = parser.parse(headerEncoded);
                    Object payloadParsed = parser.parse(payloadEncoded);
                    JSONObject headerJson = (JSONObject) headerParsed;
                    JSONObject payloadJson = (JSONObject) payloadParsed;
                    username = (String) payloadJson.get("sub");
                    currTime = String.valueOf(payloadJson.get("iat"));
                    issuer = (String) payloadJson.get("iss");
                    type =  (String) headerJson.get("type");
                    algorithm = (String) headerJson.get("alg");
                }catch (ParseException e){
                    return false;
                }
                JSONParser jsonParser = new JSONParser();
                JSONArray users = new JSONArray();
                boolean exist = false;
                boolean anyUser = true;
                if(Files.exists(Path.of(usersJsonDir))) {
                    try (FileReader reader = new FileReader(usersJsonDir)) {
                        Object obj = jsonParser.parse(reader);
                        users = (JSONArray) obj;
                        System.out.println(users);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    anyUser=false;
                }
                int i=0;
                while(i<users.size()&&!exist&&anyUser){
                    Object us = users.get(i);
                    if(((JSONObject)us).get("Username").equals(username)){
                            exist=true;
                    }
                    i++;
                }
                boolean accept = true;
                accept&=anyUser;
                accept&=exist;
                accept&=issuer.equals("Remote-Storage-Auth-Server");
                accept&=type.equals("JWT");
                accept&=algorithm.equals("HS256");
                long now = Instant.now().getEpochSecond();
                accept&=Long.parseLong(currTime)<=now;
                if(accept){
                    byte[] signatureBytes = new byte[1];
                    byte[] dot = (".").getBytes();
                    //creating array for signature
                    ByteBuffer buff = ByteBuffer.allocate(header.getBytes().length+payload.getBytes().length+dot.length);
                    buff.put(header.getBytes());
                    buff.put(dot);
                    buff.put(payload.getBytes());
                    try {
                        Mac signatureSHA = Mac.getInstance("HmacSHA256");
                        SecretKeySpec key = new SecretKeySpec(serverSecret.getBytes(),"HmacSHA256");
                        signatureSHA.init(key);
                        signatureBytes = signatureSHA.doFinal(buff.array());
                    }catch (NoSuchAlgorithmException | InvalidKeyException e){
                        System.err.println("Invalid key or not existing algo");
                    }
                    if(new String(Base64.getUrlEncoder().encode(signatureBytes)).equals(signature)){
                        try {
                            MessageDigest userDirEncipher = MessageDigest.getInstance("SHA-256");
                            userFolder = new String(userDirEncipher.digest(username.getBytes()));
                            userFolder = Base64.getUrlEncoder().encodeToString(userFolder.getBytes());
                        }catch (NoSuchAlgorithmException e){
                            System.err.println(e.getMessage());
                        }
                        return true;
                    }
                }
            }
            return false;
        }

    }

    private static class AuthHandler implements HttpHandler{
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String requestURI = exchange.getRequestURI().toString();
            String username = requestURI.substring(requestURI.indexOf("username=")+9,requestURI.indexOf("&"));
            String passwordHash = requestURI.substring(requestURI.indexOf("&pass=")+6);
            JSONObject user = new JSONObject();
            JSONArray write = new JSONArray();
            user.put("Username",new String(Base64.getDecoder().decode(username.getBytes())));
            user.put("Password",passwordHash);
            if(!Files.exists(Path.of(usersJsonDir))){
                Files.createFile(Path.of(usersJsonDir));
                try{
                    FileWriter fw = new FileWriter(usersJsonDir);
                    write.add(user);
                    fw.write(write.toJSONString());
                    fw.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
            else{
                JSONParser jsonParser = new JSONParser();
                JSONArray users = new JSONArray();
                try (FileReader reader = new FileReader(usersJsonDir))
                {
                    Object obj = jsonParser.parse(reader);
                    users = (JSONArray) obj;
                    System.out.println(users);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                boolean newUser = true;
                int i=0;
                while(i<users.size()&&newUser){
                    Object us = users.get(i);
                    if(((JSONObject)us).get("Username").equals(user.get("Username"))){
                        if(((JSONObject)us).get("Password").equals(user.get("Password"))) {
                            newUser=false;
                        }
                        else{
                            status = 403;
                            response= Base64.getEncoder().encode(("User already exist or wrong password").getBytes());
                            exchange.sendResponseHeaders(status, response.length);
                            OutputStream os = exchange.getResponseBody();
                            os.write(response);
                            os.close();
                            return;
                        }
                    }
                    i++;
                }
                if(newUser){
                    try{
                        FileWriter fw = new FileWriter(usersJsonDir);
                        users.add(user);
                        fw.write(users.toJSONString());
                        fw.close();
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }
            byte[] signatureBytes = new byte[1];
            JSONObject headerJson = new JSONObject();
            JSONObject payloadJson = new JSONObject();
            //init json header
            headerJson.put("type","JWT");
            headerJson.put("alg","HS256");
            //init json payload
            payloadJson.put("iss","Remote-Storage-Auth-Server");
            payloadJson.put("sub",new String(Base64.getDecoder().decode(username.getBytes())));
            payloadJson.put("iat", String.valueOf(Instant.now().getEpochSecond()));
            //header and payload in json encoding
            String header = headerJson.toJSONString();
            String payload = payloadJson.toJSONString();
            //base64url encoding
            byte[] b64UrlHeader = Base64.getUrlEncoder().encode(header.getBytes());
            byte[] b64UrlPayload = Base64.getUrlEncoder().encode(payload.getBytes());
            byte[] dot = (".").getBytes();
            //creating array for signature
            ByteBuffer buff = ByteBuffer.allocate(b64UrlHeader.length+b64UrlPayload.length+dot.length);
            buff.put(b64UrlHeader);
            buff.put(dot);
            buff.put(b64UrlPayload);
            //signature creating
            try {
                Mac signatureSHA = Mac.getInstance("HmacSHA256");
                SecretKeySpec key = new SecretKeySpec(serverSecret.getBytes(),"HmacSHA256");
                signatureSHA.init(key);
                signatureBytes = signatureSHA.doFinal(buff.array());
            }catch (NoSuchAlgorithmException | InvalidKeyException e){
                System.err.println("Invalid key or not existing algo");
            }
            String JWT = new String(b64UrlHeader)+"."+new String(b64UrlPayload)+"."+new String(Base64.getUrlEncoder().encode(signatureBytes));
            status = 200;
            List<String> str = new LinkedList<>();
            str.add(JWT);
            exchange.getResponseHeaders().set("Authorization-token",JWT);
            response =Base64.getEncoder().encode(("Successfully authorized. Hello,"+user.get("Username")).getBytes());
            exchange.sendResponseHeaders(status, response.length);
            OutputStream os = exchange.getResponseBody();
            os.write(response);
            os.close();
        }
    }
}
