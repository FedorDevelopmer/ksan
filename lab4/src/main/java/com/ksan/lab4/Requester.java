package com.ksan.lab4;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Requester {

    private String serverUrl;

    private String params;

    private String filepath;

    private String JWT = "default";

    private URL url;

    private boolean authorized=false;

    private HttpURLConnection connection;

    Requester(String serverUrl){
        this.serverUrl=serverUrl;
    }

    public String GetRequest(Path file){
        filepath=file.toString();
        String resp=null;
        StringBuilder response = new StringBuilder();
        int responseCode;
        BufferedReader in=null;
        params="fl_pth="+Base64.getUrlEncoder().encodeToString(filepath.getBytes())+"&true_meth=GET";
        if(this.connectURL()) {
            try {
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", "Bearer "+JWT);
                // Send the request and receive the response
                responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                } else {
                    in = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                }
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                resp = response.toString();
            } catch (IOException e) {
                System.out.println("IO exception: " + e.getMessage());
                resp=new String(Base64.getEncoder().encode(("Unable to connect to server!").getBytes()));
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        System.out.println("IO exception of closing in: " + e.getMessage());
                    }
                    in = null;
                }
            }
        }
        return resp;
    }
    public String PutFRequest(Path file,Path from){
        filepath=file.toString();
        String resp=null;
        StringBuilder response = new StringBuilder();
        int responseCode;
        BufferedReader in=null;
        params="fl_pth="+Base64.getUrlEncoder().encodeToString(filepath.getBytes());
        if(this.connectURL()) {
            try {
                connection.setRequestMethod("PUT");
                connection.setRequestProperty("Authorization", "Bearer "+JWT);
                connection.setDoOutput(true);
                byte[] sendData = Files.readAllBytes(from);
                OutputStreamWriter os = null;
                try {
                    os = new OutputStreamWriter(connection.getOutputStream());
                    //8192 -  writer buffer size by default
                    sendData = Base64.getEncoder().encode(sendData);
                    for (int i = 0; i < sendData.length; i++) {
                        os.write(sendData[i]);
                        if (i % 8191 == 0) {
                            os.flush();
                        }
                    }
                    os.flush();
                    os.close();
                } catch (IOException e) {
                    System.out.println("IO Error: " + e.getMessage());
                    resp=new String(Base64.getEncoder().encode(("Unable to connect to server!").getBytes()));
                } finally {
                    if (os != null) {
                        os = null;
                    }
                }
                // Send the request and receive the response
                responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                } else {
                    in = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                }
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                resp = response.toString();
                byte[] respBytes = resp.getBytes();
                respBytes = Base64.getDecoder().decode(respBytes);
                resp = new String(respBytes);

            } catch (IOException e) {
                System.out.println("IO exception: " + e.getMessage());
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        System.out.println("IO exception of closing in: " + e.getMessage());
                    }
                    in = null;
                }
            }
        }
        return resp;
    }

    public String PutRequest(Path file,String data){
        filepath=file.toString();
        String resp=null;
        StringBuilder response = new StringBuilder();
        int responseCode;
        BufferedReader in=null;
        params="fl_pth="+Base64.getUrlEncoder().encodeToString(filepath.getBytes());
        if(this.connectURL()) {
            try {
                connection.setRequestMethod("PUT");
                connection.setRequestProperty("Authorization", "Bearer "+JWT);
                connection.setDoOutput(true);
                WriteData(data);
                // Send the request and receive the response
                responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                } else {
                    in = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                }
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                resp = response.toString();
                byte[] respBytes = resp.getBytes();
                respBytes = Base64.getDecoder().decode(respBytes);
                resp = new String(respBytes);

            } catch (IOException e) {
                System.out.println("IO exception: " + e.getMessage());
                resp="Unable to connect to server!";
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        System.out.println("IO exception of closing in: " + e.getMessage());
                    }
                    in = null;
                }
                System.gc();

            }
        }
        return resp;
    }

    public String PostRequest(Path file,String data){
        filepath=file.toString();
        String resp=null;
        StringBuilder response = new StringBuilder();
        int responseCode;
        BufferedReader in=null;
        params="fl_pth="+Base64.getUrlEncoder().encodeToString(filepath.getBytes());
        params=params.replace("\\","*");
        if(this.connectURL()) {
            try {
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Authorization", "Bearer "+JWT);
                connection.setDoOutput(true);
                WriteData(data);
                // Send the request and receive the response
                responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                } else {
                    in = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                }
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                resp = response.toString();
                byte[] respBytes = resp.getBytes();
                respBytes = Base64.getDecoder().decode(respBytes);
                resp = new String(respBytes);

            } catch (IOException e) {
                System.out.println("IO exception: " + e.getMessage());
                resp="Unable to connect to server!";
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        System.out.println("IO exception of closing in: " + e.getMessage());
                    }
                    in = null;
                }
            }
        }
        return resp;
    }
    public String DeleteRequest(Path file){
        filepath=file.toString();
        String resp=null;
        StringBuilder response = new StringBuilder();
        int responseCode=0;
        BufferedReader in=null;
        params="fl_pth="+Base64.getUrlEncoder().encodeToString(filepath.getBytes());
        params=params.replace("\\","*");
        if(this.connectURL()) {
            try {
                connection.setRequestMethod("DELETE");
                connection.setRequestProperty("Authorization", "Bearer "+JWT);
                // Send the request and receive the response
                responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                } else {
                    in = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                }
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                resp = response.toString();
                byte[] respBytes = resp.getBytes();
                respBytes = Base64.getDecoder().decode(respBytes);
                resp = new String(respBytes);

            } catch (IOException e) {
                System.out.println("IO exception: " + e.getMessage());
                resp="Unable to connect to server!";
            } finally {
                if (connection != null) {
                    connection.disconnect();
                    connection = null;
                }
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        System.out.println("IO exception of closing in: " + e.getMessage());
                    }
                    in = null;
                }
            }
        }
        return resp;
    }
    public String CopyRequest(Path file,Path pathTo){
        filepath=file.toString();
        String filepathTo=pathTo.toString();
        StringBuilder response = new StringBuilder();
        String resp=null;
        int responseCode=0;
        BufferedReader in=null;
        params="fl_pth="+Base64.getUrlEncoder().encodeToString(filepath.getBytes())+"&fl_to="+Base64.getUrlEncoder().encodeToString(filepathTo.getBytes())+"&true_meth=COPY";
        if(this.connectURL()) {
            try {
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", "Bearer "+JWT);
                // Send the request and receive the response
                responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                } else {
                    in = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                }
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                resp = response.toString();
                byte[] respBytes = resp.getBytes();
                respBytes = Base64.getDecoder().decode(respBytes);
                resp = new String(respBytes);

            } catch (IOException e) {
                System.out.println("IO exception: " + e.getMessage());
                resp="Unable to connect to server!";
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        System.out.println("IO exception of closing in: " + e.getMessage());
                    }
                    in = null;
                }
            }
        }
        return resp;
    }
    public String MoveRequest(Path file,Path pathTo){
        filepath=file.toString();
        String filepathTo=pathTo.toString();
        StringBuilder response = new StringBuilder();
        String resp=null;
        int responseCode=0;
        BufferedReader in=null;
        params="fl_pth="+Base64.getUrlEncoder().encodeToString(filepath.getBytes())+"&fl_to="+Base64.getUrlEncoder().encodeToString(filepathTo.getBytes())+"&true_meth=MOVE";
        if(this.connectURL()) {
            try {
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", "Bearer "+JWT);
                // Send the request and receive the response
                responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                } else {
                    in = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                }
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                resp = response.toString();
                byte[] respBytes = resp.getBytes();
                respBytes = Base64.getDecoder().decode(respBytes);
                resp = new String(respBytes);

            } catch (IOException e) {
                System.out.println("IO exception: " + e.getMessage());
                resp="Unable to connect to server!";
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        System.out.println("IO exception of closing in: " + e.getMessage());
                    }
                    in = null;
                }
            }
        }
        return resp;
    }
    public void WriteData(String data){
        OutputStreamWriter os=null;
        try {
            os = new OutputStreamWriter(connection.getOutputStream());
            //8192 -  writer buffer size by default
            byte[] sendData = data.getBytes();
            sendData = Base64.getEncoder().encode(sendData);
            for (int i = 0; i < sendData.length; i++) {
                os.write(sendData[i]);
                if (i % 8191 == 0) {
                    os.flush();
                }
            }
            os.flush();
            os.close();
        }catch(IOException e){
            System.out.println("IO Error: "+e.getMessage());
        }finally {
            if(os!=null){
                os=null;
            }
            System.gc();
        }
    }

    public boolean connectURL(){
        // Construct the URL for the request
        try {
            url = new URL(serverUrl+"?"+params);
        }
        catch(MalformedURLException e){
            System.out.println("Incorrect url: "+e.getMessage());
            return false;
        }
        try {
            connection = (HttpURLConnection) url.openConnection();
            return true;
        }
        catch (IOException e){
            System.out.println("IO exception: "+e.getMessage());
            return false;
        }
    }
    public void disconnectURL(){
        if(connection!=null) {
            connection.disconnect();
            connection=null;
        }
    }
    public int getResponseCode(){
        if(connection!=null){
            try {
               return connection.getResponseCode();
            }
            catch (IOException e){
                System.err.println("IO exception on response code:"+e.getMessage());
            }
        }
        return 0;

    }
    public void setJWT(String token){
        this.JWT = token;
    }
    public int getAuthPort(){
        int result = -1;
        if(connection.getHeaderFields().get("Auth-port")!=null&&connection.getHeaderFields().get("Auth-port").get(0)!=null) {
            result=Integer.parseInt(connection.getHeaderFields().get("Auth-port").get(0));
        }
        return result;
    }

}
