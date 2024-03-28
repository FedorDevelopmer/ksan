package com.course.filetransfer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class FileTransfer extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("FTPSimple");
        stage.setScene(scene);
        stage.setOnShown(e -> {
            VBox mainContainer = (VBox) scene.lookup("#mainContainer");
            Controller cont = fxmlLoader.getController();
            cont.mainScene =scene;
            cont.loadTranslationJSON();
            // Get the available screen bounds
            double screenWidth = Screen.getPrimary().getVisualBounds().getWidth();
            double screenHeight = Screen.getPrimary().getVisualBounds().getHeight();
            // Calculate the scene width and height based on the preferred width and height
            double sceneWidth = Math.min(mainContainer.getPrefWidth(), screenWidth);
            double sceneHeight = Math.min(mainContainer.getPrefHeight(), screenHeight);
            // Set the scene dimensions
            stage.setWidth(sceneWidth);
            stage.setHeight(sceneHeight);
        });
        stage.show();
    }
    public static void main(String[] args) {
        launch();
    }
}