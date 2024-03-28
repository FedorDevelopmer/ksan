package com.ksan.lab3;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Base64;

public class Requester {

    private String serverUrl;

    private String filepath;

    private URL url;

    private HttpURLConnection connection;

    Requester(String serverUrl){
        this.serverUrl=serverUrl;
    }

    public String GetRequest(Path file){
        filepath=file.toString();
        String resp=null;
        StringBuilder response = new StringBuilder();
        int responseCode=0;
        BufferedReader in=null;
        String params="fl_pth="+filepath+"&true_meth=GET";
        params=params.replace("\\","*");
        // Construct the URL for the request
        try {
            String fullUrl = serverUrl;
            url = new URL(fullUrl+"?"+params);
        }
        catch(MalformedURLException e){
            System.out.println("Incorrect url: "+e.getMessage());
            return null;
        }
        try {
            // Create the HTTP GET request
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            // Send the request and receive the response
            responseCode = connection.getResponseCode();
            if (responseCode==HttpURLConnection.HTTP_OK){
                // Read the response from the server
                in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                resp = response.toString();
            }

        }
        catch (IOException e){
            System.out.println("IO exception: "+e.getMessage());
        }finally {
            if(connection!=null) {
                connection.disconnect();
                connection=null;
            }
            if(in!=null){
                try{
                    in.close();
                }
                catch (IOException e){
                    System.out.println("IO exception of closing in: "+e.getMessage());
                }
                in=null;
            }
            System.gc();
        }
        return resp;
    }
    public String PutFRequest(Path file,Path from){
        filepath=file.toString();
        String resp=null;
        StringBuilder response = new StringBuilder();
        int responseCode=0;
        BufferedReader in=null;
        String params="fl_pth="+filepath;
        params=params.replace("//","*");
        // Construct the URL for the request
        try {
            String fullUrl = serverUrl;
            url = new URL(fullUrl+"?"+params);
        }
        catch(MalformedURLException e){
            System.out.println("Incorrect url: "+e.getMessage());
            return null;
        }
        try {
            // Create the HTTP GET request
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("PUT");
            connection.setDoOutput(true);
            byte[] sendData = Files.readAllBytes(from);
            OutputStreamWriter os=null;
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
            }catch(IOException e){
                System.out.println("IO Error: "+e.getMessage());
            }finally {
                if(os!=null){
                    os=null;
                }
                System.gc();
            }
            // Send the request and receive the response
            responseCode = connection.getResponseCode();
            if (responseCode==HttpURLConnection.HTTP_OK){
                // Read the response from the server
                in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                resp = response.toString();
                byte[] respBytes = resp.getBytes();
                respBytes=Base64.getDecoder().decode(respBytes);
                resp=new String(respBytes);
            }

        }
        catch (IOException e){
            System.out.println("IO exception: "+e.getMessage());
        }finally {
            if(connection!=null) {
                connection.disconnect();
                connection=null;
            }
            if(in!=null){
                try{
                    in.close();
                }
                catch (IOException e){
                    System.out.println("IO exception of closing in: "+e.getMessage());
                }
                in=null;
            }
            System.gc();

        }
        return resp;
    }

    public String PutRequest(Path file,String data){
        filepath=file.toString();
        String resp=null;
        StringBuilder response = new StringBuilder();
        int responseCode=0;
        BufferedReader in=null;
        String params="fl_pth="+filepath;
        params=params.replace("\\","*");
        // Construct the URL for the request
        try {
            String fullUrl = serverUrl;
            url = new URL(fullUrl+"?"+params);
        }
        catch(MalformedURLException e){
            System.out.println("Incorrect url: "+e.getMessage());
            return null;
        }
        try {
            // Create the HTTP GET request
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("PUT");
            connection.setDoOutput(true);
            WriteData(data);
            // Send the request and receive the response
            responseCode = connection.getResponseCode();
            if (responseCode==HttpURLConnection.HTTP_OK){
                // Read the response from the server
                in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                resp = response.toString();
                byte[] respBytes = resp.getBytes();
                respBytes=Base64.getDecoder().decode(respBytes);
                resp=new String(respBytes);
            }

        }
        catch (IOException e){
            System.out.println("IO exception: "+e.getMessage());
        }finally {
            if(connection!=null) {
                connection.disconnect();
                connection=null;
            }
            if(in!=null){
                try{
                    in.close();
                }
                catch (IOException e){
                    System.out.println("IO exception of closing in: "+e.getMessage());
                }
                in=null;
            }
            System.gc();

        }
        return resp;
    }

    public String PostRequest(Path file,String data){
        filepath=file.toString();
        String resp=null;
        StringBuilder response = new StringBuilder();
        int responseCode=0;
        BufferedReader in=null;
        String params="fl_pth="+filepath;
        params=params.replace("\\","*");
        // Construct the URL for the request
        try {
            String fullUrl = serverUrl;
            url = new URL(fullUrl+"?"+params);
        }
        catch(MalformedURLException e){
            System.out.println("Incorrect url: "+e.getMessage());
            return null;
        }
        try {
            // Create the HTTP GET request
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            WriteData(data);
            // Send the request and receive the response
            responseCode = connection.getResponseCode();
            if (responseCode==HttpURLConnection.HTTP_OK){
                // Read the response from the server
                in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                resp = response.toString();
                byte[] respBytes = resp.getBytes();
                respBytes=Base64.getDecoder().decode(respBytes);
                resp=new String(respBytes);
            }

        }
        catch (IOException e){
            System.out.println("IO exception: "+e.getMessage());
        }finally {
            if(connection!=null) {
                connection.disconnect();
                connection=null;
            }
            if(in!=null){
                try{
                    in.close();
                }
                catch (IOException e){
                    System.out.println("IO exception of closing in: "+e.getMessage());
                }
                in=null;
            }
            System.gc();

        }

        return resp;
    }
    public String DeleteRequest(Path file){
        filepath=file.toString();
        String resp=null;
        StringBuilder response = new StringBuilder();
        int responseCode=0;
        BufferedReader in=null;
        String params="fl_pth="+filepath;
        params=params.replace("\\","*");
        // Construct the URL for the request
        try {
            String fullUrl = serverUrl;
            url = new URL(fullUrl+"?"+params);
        }
        catch(MalformedURLException e){
            System.out.println("Incorrect url: "+e.getMessage());
            return null;
        }
        try {
            // Create the HTTP GET request
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("DELETE");
            // Send the request and receive the response
            responseCode = connection.getResponseCode();
            if (responseCode==HttpURLConnection.HTTP_OK){
                // Read the response from the server
                in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                resp = response.toString();
                byte[] respBytes = resp.getBytes();
                respBytes=Base64.getDecoder().decode(respBytes);
                resp=new String(respBytes);
            }

        }
        catch (IOException e){
            System.out.println("IO exception: "+e.getMessage());
        }finally {
            if(connection!=null) {
                connection.disconnect();
                connection=null;
            }
            if(in!=null){
                try{
                    in.close();
                }
                catch (IOException e){
                    System.out.println("IO exception of closing in: "+e.getMessage());
                }
                in=null;
            }
            System.gc();

        }

        return resp;
    }
    public String CopyRequest(Path file,Path path_to){
        filepath=file.toString();
        String filepath_to=path_to.toString();
        StringBuilder response = new StringBuilder();
        String resp=null;
        int responseCode=0;
        BufferedReader in=null;
        String params="fl_pth="+filepath+"&fl_to="+filepath_to+"&true_meth=COPY";
        params=params.replace("\\","*");
        // Construct the URL for the request
        try {
            String fullUrl = serverUrl;
            url = new URL(fullUrl+"?"+params);
        }
        catch(MalformedURLException e){
            System.out.println("Incorrect url: "+e.getMessage());
            return null;
        }
        try {
            // Create the HTTP GET request
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            // Send the request and receive the response
            responseCode = connection.getResponseCode();
            if (responseCode==HttpURLConnection.HTTP_OK){
                // Read the response from the server
                in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                resp = response.toString();
                byte[] respBytes = resp.getBytes();
                respBytes=Base64.getDecoder().decode(respBytes);
                resp=new String(respBytes);
            }

        }
        catch (IOException e){
            System.out.println("IO exception: "+e.getMessage());
        }finally {
            if(connection!=null) {
                connection.disconnect();
                connection=null;
            }
            if(in!=null){
                try{
                    in.close();
                }
                catch (IOException e){
                    System.out.println("IO exception of closing in: "+e.getMessage());
                }
                in=null;
            }
            System.gc();

        }

        return resp;
    }
    public String MoveRequest(Path file,Path path_to){
        filepath=file.toString();
        String filepath_to=path_to.toString();
        StringBuilder response = new StringBuilder();
        String resp=null;
        int responseCode=0;
        BufferedReader in=null;
        String params="fl_pth="+filepath+"&fl_to="+filepath_to+"&true_meth=MOVE";
        params=params.replace("\\","*");
        // Construct the URL for the request
        try {
            String fullUrl = serverUrl;
            url = new URL(fullUrl+"?"+params);
        }
        catch(MalformedURLException e){
            System.out.println("Incorrect url: "+e.getMessage());
            return null;
        }
        try {
            // Create the HTTP GET request
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            // Send the request and receive the response
            responseCode = connection.getResponseCode();
            if (responseCode==HttpURLConnection.HTTP_OK){
                // Read the response from the server
                in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                resp = response.toString();
                byte[] respBytes = resp.getBytes();
                respBytes=Base64.getDecoder().decode(respBytes);
                resp=new String(respBytes);

            }

        }
        catch (IOException e){
            System.out.println("IO exception: "+e.getMessage());
        }finally {
            if(connection!=null) {
                connection.disconnect();
                connection=null;
            }
            if(in!=null){
                try{
                    in.close();
                }
                catch (IOException e){
                    System.out.println("IO exception of closing in: "+e.getMessage());
                }
                in=null;
            }
            System.gc();

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

}
