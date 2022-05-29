package com.farkasch.barista.gui.component;


import com.farkasch.barista.util.Result;
import com.farkasch.barista.util.enums.ResultTypeEnum;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javax.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class ErrorPopup extends Stage {

  private Text message;
  private Button acceptButton;
  private Button errorLogButton;
  private BorderPane windowLayout;
  private HBox textLayout;
  private GridPane buttonLayout;

  private Scene scene;
  private File errorLog;

  @PostConstruct
  private void init(){
    setTitle("Error");
    message = new Text();
    acceptButton = new Button("Ok");
    errorLogButton = new Button("Error Log");
    textLayout = new HBox(message);
    buttonLayout = new GridPane();
    windowLayout = new BorderPane();
    errorLog = null;

    scene = new Scene(windowLayout, 300, 100);
    scene.getStylesheets().add("style.css");

    windowLayout.setCenter(textLayout);
    windowLayout.setBottom(buttonLayout);

    buttonLayout.addColumn(0, errorLogButton);
    buttonLayout.addColumn(1, acceptButton);
    buttonLayout.setHgap(5);
    buttonLayout.setAlignment(Pos.BOTTOM_RIGHT);
    BorderPane.setMargin(buttonLayout, new Insets(10));

    windowLayout.setMinHeight(scene.getHeight());
    windowLayout.setMinWidth(scene.getWidth());
    windowLayout.setMaxWidth(Double.MAX_VALUE);
    windowLayout.setMaxHeight(Double.MAX_VALUE);

    textLayout.setAlignment(Pos.CENTER);

    acceptButton.setOnAction(click -> close());
    errorLogButton.setOnAction(click -> {
      try{
        Runtime.getRuntime().exec("explorer.exe /select," + errorLog.getAbsolutePath());
      } catch(IOException e){
        e.printStackTrace();
      }
    });

    initModality(Modality.APPLICATION_MODAL);
    setResizable(false);
  }

  public void showWindow(Result result){
    if(result.getResult().equals(ResultTypeEnum.ERROR)){
      message.setText(result.getMessage());
      if(result.getLogFile() != null){
        errorLogButton.setVisible(true);
        errorLog = result.getLogFile();
      } else {
        errorLogButton.setVisible(false);
      }
    }
    setScene(scene);
    show();
  }
}
