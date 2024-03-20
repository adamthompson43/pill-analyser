package com.example.pillanalyser;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("pill-analyser.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Pill Analyser");
        stage.setScene(scene);
        stage.show();
        stage.setMinWidth(stage.getWidth());
        stage.setMinHeight(stage.getHeight());
        stage.setResizable(false);
    }

    public static void main(String[] args) {
        launch();
    }


}