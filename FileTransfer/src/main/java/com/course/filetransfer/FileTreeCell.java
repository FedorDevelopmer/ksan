package com.course.filetransfer;

import javafx.scene.control.TreeCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import java.nio.file.Files;
import java.nio.file.Path;


//file cell needs full path as string to properly display
public class FileTreeCell extends TreeCell<String> {
    private  ImageView imageView;
    private HBox hbox;
    private String fullPath;

    private HistoryItem remote;

    public FileTreeCell(HistoryItem remote,String path) {
        imageView = new ImageView();
        imageView.setFitWidth(20);
        imageView.setFitHeight(20);
        hbox = new HBox(10);
        hbox.getChildren().add(imageView);
        this.remote = remote;
        Image image;
        fullPath = path;
        if (remote == null) {
            if (Files.isDirectory(Path.of(fullPath))) {
                image = new Image(getClass().getResource("images/dir.png").toExternalForm());
            } else {
                image = new Image(getClass().getResource("images/file_icon.png").toExternalForm());
            }
        }else{
            if (FileTreeWork.isDirectoryFtp(fullPath, remote)) {
                image = new Image(getClass().getResource("images/dir.png").toExternalForm());
            } else {
                image = new Image(getClass().getResource("images/file_icon.png").toExternalForm());
            }
        }
        imageView.setImage(image);
        // Set the text for the ListCell
        setText(path.substring(path.lastIndexOf("\\")+1));
        setGraphic(hbox);
    }
}