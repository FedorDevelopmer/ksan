package com.course.filetransfer;

import javafx.scene.control.ListView;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPCmd;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPSClient;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSessionContext;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;


public class FileTreeWork {

    public static  TreeItem<String> addChildren(TreeItem<String> tree) {
        String filePath = FileTreeWork.formFullFilePath(tree);
        File main = new File(filePath);
        File[] children = main.listFiles();
        if (children != null) {
            for (File child : children) {
                String childName = child.toString();
                TreeItem<String> currChild = new TreeItem<>(childName);
                if (Files.isDirectory(Path.of(childName))) {
                    SetImageTree(currChild, "images/dir.png", 20, 20);
                } else {
                    SetImageTree(currChild, "images/file_icon.png", 15, 15);
                }
                if(childName.lastIndexOf("\\")!=childName.length()-1) {
                    currChild.setValue(childName.substring(childName.lastIndexOf("\\") + 1));
                }
                boolean contains = false;
                for(TreeItem<String> item:tree.getChildren()) {
                    if (item.getValue().equals(currChild.getValue())) {
                        contains=true;
                    }
                }
                if(!contains) {
                    tree.getChildren().add(currChild);
                }
            }
            boolean contains = false;
            List<TreeItem<String>> childrenToDelete = new ArrayList<>();
            for(TreeItem<String> child:tree.getChildren()){
                for(File file:children){
                    if(file.getName().equals(child.getValue())){
                        contains=true;
                    }
                }
                if(!contains){
                    childrenToDelete.add(child);
                }
                contains=false;
            }
            tree.getChildren().removeAll(childrenToDelete);
        }
        return tree;
    }
    public static  TreeItem<String> addRemoteChildren(TreeItem<String> tree, HistoryItem connectionData) {
        String filePath = FileTreeWork.formFullFilePathRemote(tree);
        try {
            if(isDirectoryFtp(filePath,connectionData)) {
                FTPClient client;
                client = FTP.createClient(connectionData.getIp(),connectionData.getPort(),connectionData.getUser(),connectionData.getPass());
                client.changeWorkingDirectory(filePath);
                client.enterLocalActiveMode();
                FTPFile[] children = client.listFiles(filePath);
                if (children != null) {
                    for (FTPFile child : children) {
                        String childName = child.getName();
                        childName = childName.substring(childName.lastIndexOf("\\") + 1);
                        TreeItem<String> currChild = new TreeItem<>(childName);
                        if (child.isDirectory()) {
                            SetImageTree(currChild, "images/dir.png", 20, 20);
                        } else {
                            SetImageTree(currChild, "images/file_icon.png", 15, 15);
                        }
                        boolean contains = false;
                        for(TreeItem<String> item:tree.getChildren()) {
                            if (item.getValue().equals(currChild.getValue())) {
                                contains=true;
                            }
                        }
                        if(!contains) {
                            tree.getChildren().add(currChild);
                        }
                    }
                    boolean contains = false;
                    List<TreeItem<String>> childrenToDelete = new ArrayList<>();
                    for(TreeItem<String> child:tree.getChildren()){
                        for(FTPFile file:children){
                            if(file.getName().equals(child.getValue())){
                                contains=true;
                            }
                        }
                        if(!contains){
                            childrenToDelete.add(child);
                        }
                        contains=false;

                    }
                    tree.getChildren().removeAll(childrenToDelete);
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return tree;
    }
    public static void addListElement(ListView view, TreeItem<String> element, HistoryItem connectionData) {
        try {
            String filePath = FileTreeWork.formFullFilePathRemote(element);
            FTPClient client = FTP.createClient(connectionData.getIp(),connectionData.getPort(),connectionData.getUser(),connectionData.getPass());
            client.changeWorkingDirectory(filePath);
            client.enterLocalActiveMode();
            FileListCell listItem = new FileListCell(null,filePath,view.getPrefWidth());
            view.getItems().add(listItem);
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    public static void addListElementRemote(ListView view, TreeItem<String> element, HistoryItem connectionData) {
        try {
            String filePath = FileTreeWork.formFullFilePathRemote(element);
            FTPClient client = FTP.createClient(connectionData.getIp(),connectionData.getPort(),connectionData.getUser(),connectionData.getPass());
            client.changeWorkingDirectory(filePath);
            client.enterLocalActiveMode();
            FileListCell listItem = new FileListCell(connectionData,filePath,view.getPrefWidth());
            view.getItems().add(listItem);
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    public static void SetImageTree(TreeItem<String> root, String filePath, int width, int height){
        Image icon = new Image(FileTreeWork.class.getResource(filePath).toExternalForm());
        ImageView view = new ImageView(icon);
        view.setFitWidth(width);
        view.setFitHeight(height);
        root.setGraphic(view);
    }
    public static String formFullFilePath(TreeItem<String> tree){
        String uri="";
        while(tree!=null) {
            if ((tree.getValue().contains("/")||tree.getValue().contains("\\")||uri.isEmpty())) {
                uri = tree.getValue().concat(uri);
            } else {
                uri = tree.getValue().concat("/" + uri);
            }
            tree = tree.getParent();

        }
        return uri;
    }
    public static String formFullFilePathRemote(TreeItem<String> tree){
        String uri="";
            while(tree!=null&&tree.getValue()!=null) {
                if ((tree.getValue().contains("/")||tree.getValue().contains("\\")||uri.isEmpty())) {
                    uri = tree.getValue().concat(uri);
                } else {
                    uri = tree.getValue().concat("/" + uri);
                }
                tree = tree.getParent();
            }
        return uri;
    }
    public static boolean isDirectoryFtp(String path,HistoryItem remote){
        try {
            FTPClient client = FTP.createClient(remote.getIp(),remote.getPort(),remote.getUser(),remote.getPass());
            boolean result = client.changeWorkingDirectory(path);
                if(result) {
                    return true;
                }
                else{
                    return false;
                }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    public static String getElementName(String fullPath){
        int indexSlash = fullPath.lastIndexOf("\\");
        int indexBSlash = fullPath.lastIndexOf("/");
        String name = fullPath.substring(fullPath.lastIndexOf(fullPath.charAt(Math.max(indexSlash,indexBSlash)))+1);
        return name;
    }
    //why not only name? multiple directories and files with same names
    public static TreeItem<String> findTreeItemByNameAndPath(TreeItem<String> root,String value,String fullPath){
        if (root.getValue().equals(value)&&FileTreeWork.formFullFilePath(root).equals(fullPath)) {
            return root;
        }
        for (TreeItem<String> child : root.getChildren()) {
            TreeItem<String> result = findTreeItemByNameAndPath(child, value,fullPath);
            if (result != null) {
                return result;
            }
        }
        return null;
    }


}
