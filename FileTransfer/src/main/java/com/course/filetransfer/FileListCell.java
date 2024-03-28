package com.course.filetransfer;

import javafx.geometry.Insets;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import org.apache.commons.net.ftp.FTPClient;
import java.nio.file.Files;
import java.nio.file.Path;


//file cell needs full path as string to properly display
public class FileListCell extends ListCell<String> {
    private  ImageView imageView;
    private HBox hbox;
    private String fullPath;

    private HistoryItem remote;

    public FileListCell(HistoryItem remote,String path,double parentWidth) {
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
        setText(FileTreeWork.getElementName(fullPath));
        setPadding(new Insets(0));
        Text text = new Text(getText());
        double width = text.getLayoutBounds().getWidth()+30;
        setPrefWidth(width);
        setGraphic(hbox);
    }
    public String getFullPath(){
        return fullPath;
    }
}