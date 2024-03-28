package com.course.filetransfer;

import com.ibm.icu.text.Transliterator;
import javafx.application.Platform;
import javafx.event.*;
import javafx.scene.control.TextArea;
import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.ftp.FTPClient;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class FTP implements EventDispatcher {

    private static Controller contCaller;

    protected FTPClient remote;

    FTP(Controller ctrl){
        contCaller = ctrl;
    }
      public FTPClient loginFTP(String host, String port, String login, String pass) throws Exception{
          FTPClient client;
          client = FTP.createClient(host,port,login,pass);
          if(client!=null) {
              remote = client;
              HistoryItem newUser = new HistoryItem(host, port, login, pass);
              if (!Files.exists(contCaller.historyPath)) {
                  Files.createFile(contCaller.historyPath);
                  List<HistoryItem> historyItems = new ArrayList<>();
                  historyItems.add(newUser);
                  Serializer.serialize(historyItems, contCaller.historyPath);
              } else {
                  List<HistoryItem> historyItems = (List<HistoryItem>) Serializer.deserialize(contCaller.historyPath);
                  if (!historyItems.contains(newUser)) {
                      if (historyItems.size() < 10) {
                          historyItems.add(newUser);
                      } else {
                          for (int i = historyItems.size(); i > 0; i--) {
                              historyItems.set(i, historyItems.get(i - 1));
                          }
                          historyItems.set(0, newUser);
                      }
                  }
                  Serializer.serialize(historyItems, contCaller.historyPath);
              }
          }
          return client;
      }
    public static boolean createDirectory(HistoryItem connectData,String path)throws IOException{
        boolean created=false;
        FTPClient client=createClient(connectData.getIp(),connectData.getPort(),connectData.getUser(),connectData.getPass());
        client.setControlEncoding(StandardCharsets.UTF_8.name());
        String[] dirParts = path.split("/");
        StringBuilder directoryFormer = new StringBuilder();
        for(String part:dirParts){
            if(!part.isEmpty()){
                directoryFormer.append(part).append("/");
                System.out.println(client.getReplyString());
                byte[] directoryName = directoryFormer.toString().getBytes(StandardCharsets.UTF_8);
                String str = new String(directoryName,StandardCharsets.UTF_8);
                //in case of file with russian symbols
                Transliterator transliterator = Transliterator.getInstance("Russian-Latin/BGN");
                String strEng = transliterator.transliterate(str);
                strEng=strEng.replaceAll("№","N");
                strEng=strEng.replaceAll("ʹ","");
                strEng=strEng.replaceAll("ё","");
                created = client.makeDirectory(strEng);
                client.changeWorkingDirectory(directoryFormer.toString());
                if(created){
                    System.out.println("Directory "+directoryFormer+" created successfully");
                }
                else{
                    System.out.println("Directory was not created"+client.getReplyString());
                }
                directoryFormer.setLength(0);
            }
        }
        client.logout();
        client.disconnect();
        return created;
    }
    public static FTPClient createClient(String host, String port, String login, String pass){
        FTPClient client = new FTPClient();
        Platform.runLater(()->{
            contCaller.connectProgress.setProgress(-1);
        });
        try {
            client.addProtocolCommandListener(new ProtocolCommandListener() {
                @Override
                public void protocolCommandSent(ProtocolCommandEvent protocolCommandEvent) {
                    System.out.printf("Command sent: [%s]-%s%n", protocolCommandEvent.getCommand(), protocolCommandEvent.getMessage());
                }

                @Override
                public void protocolReplyReceived(ProtocolCommandEvent protocolCommandEvent) {
                    System.out.printf("Command sent: [%s]-%s%n", protocolCommandEvent.getCommand(), protocolCommandEvent.getMessage());
                }
            });
            client.setAutodetectUTF8(true);
            client.setPassiveNatWorkaround(false);
            if(correctConnectionData(host,port,login,pass)) {
                client.connect(host, Integer.parseInt(port));
                client.login(login, pass);
            }else{
                System.out.println("Incorrect connection data.");
                client=null;
            }
        }catch (Exception e){
            System.out.println("Connection error");
        }
        return client;
    }
    public static boolean correctConnectionData(String host,String port,String name,String pass){
        boolean correct=true;
        try {
            if(!(host.isEmpty()||port.isEmpty()||name.isEmpty()||pass.isEmpty())) {
                if (host.contains(".")) {
                    String[] ipParts = host.split("\\.");
                    if (ipParts.length == 4) {
                        int partValue = 0;
                        for (String part : ipParts) {
                            partValue = Integer.parseInt(part);
                            if (partValue < 0 || partValue > 255) {
                                correct = false;
                            }
                        }
                    } else {
                        correct = false;
                    }
                } else {
                    correct = false;
                }
                int portValue = Integer.parseInt(port);
                if (portValue < 0 || portValue > 65535) {
                    correct = false;
                }
            }else{
                correct=false;
            }
        }catch (Exception e){
            correct=false;
        }
        return correct;
    }
    @Override
    public Event dispatchEvent(Event event, EventDispatchChain eventDispatchChain) {
        return (Event) eventDispatchChain.prepend(this);
    }
}
