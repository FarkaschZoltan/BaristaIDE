package com.farkasch.barista.GUI;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MainApp extends Application {

    public static void main(String args[]){
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("BaristaIDE");

        TextArea sideMenuDummy = new TextArea();
        TextArea codeArea = new TextArea();
        TextField topMenuDummy = new TextField();

        BorderPane layout = new BorderPane();
        layout.setCenter(codeArea);
        layout.setTop(topMenuDummy);
        layout.setLeft(sideMenuDummy);

        Scene scene = new Scene(layout, 700, 600);

        ((TextArea)layout.getLeft()).setPrefWidth(scene.getWidth() * 0.2);

        stage.setScene(scene);

        stage.show();
    }
}
