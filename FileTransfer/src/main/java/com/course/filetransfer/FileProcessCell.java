package com.course.filetransfer;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

//format for transfering element data: "filepath:progressValue:progressPercent";
public class FileProcessCell extends ListCell<String> {
    private HBox container;

    private ProgressBar progress;

    private Label filePath;

    private Label progressPercents;

    FileProcessCell(){
        container = new HBox();
        progress=new ProgressBar();
        progress.setStyle("-fx-accent: #00ff80;");
        progressPercents=new Label();
        filePath = new Label();
        container.setPrefWidth(400);
        progress.setPrefWidth(200);
        HBox progressContainer = new HBox();
        filePath.setMaxWidth(100);
        progressContainer.getChildren().addAll(progress,progressPercents);
        container.getChildren().addAll(filePath,progressContainer);
        HBox.setHgrow(container, Priority.ALWAYS);
        progress.setProgress(10.0/100.0);
        progressContainer.setSpacing(25);
        container.setSpacing(100);
        HBox.setMargin(filePath,new Insets(0,0,0,20));
        setGraphic(container);
    }
    @Override
    public void updateItem(String item,boolean empty){
        super.updateItem(item,empty);
        if(empty||item==null){
            setText(null);
            setGraphic(null);
        }
        else{
            String[] con = item.split(":");
            filePath.setText(con[0]);
            double progressDouble = Double.parseDouble(con[1]);

            long progressLong = (long)(progressDouble*100);
            if(progressLong>100){
                progressPercents.setText("100%");
                progress.setProgress(1);
            } else{
                progressPercents.setText(progressLong+"%");
                progress.setProgress(progressDouble);
            }

            setGraphic(container);
        }

    }
}
