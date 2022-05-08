package com.farkasch.barista.gui.component;

import com.farkasch.barista.util.Result;
import java.nio.file.Paths;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javax.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class WarningPopup extends Stage {

  private  Text message;
  private  Button acceptButton;
  private  Button cancelButton;
  private  BorderPane windowLayout;
  private  HBox textLayout;
  private  GridPane buttonLayout;
  private  Scene scene;
  private  EventHandler<ActionEvent> acceptButtonClick;
  private  EventHandler<ActionEvent> cancelButtonClick;

  @PostConstruct
  private void init(){
    message = new Text();
    acceptButton = new Button("Ok");
    cancelButton = new Button("Cancel");
    buttonLayout = new GridPane();
    textLayout = new HBox(message);
    windowLayout = new BorderPane();
    acceptButtonClick = null;
    cancelButtonClick = null;

    scene = new Scene(windowLayout, 300, 100);
    scene.getStylesheets().add(Paths.get("src/main/java/com/farkasch/barista/style.css").toAbsolutePath().toUri().toString());

    message.setWrappingWidth(scene.getWidth() - 20);
    message.setTextAlignment(TextAlignment.CENTER);

    windowLayout.setCenter(textLayout);
    windowLayout.setBottom(buttonLayout);

    buttonLayout.addColumn(0, cancelButton);
    buttonLayout.addColumn(1, acceptButton);
    buttonLayout.setHgap(10);
    buttonLayout.setAlignment(Pos.CENTER_RIGHT);
    BorderPane.setMargin(buttonLayout, new Insets(10));

    windowLayout.setMinHeight(scene.getHeight());
    windowLayout.setMinWidth(scene.getWidth());
    windowLayout.setMaxWidth(Double.MAX_VALUE);
    windowLayout.setMaxHeight(Double.MAX_VALUE);

    textLayout.setAlignment(Pos.CENTER);
    BorderPane.setMargin(textLayout, new Insets(10));

    acceptButton.setOnAction(click -> {
      if(acceptButtonClick != null){
        acceptButtonClick.handle(click);
      }
      close();
    });
    cancelButton.setOnAction(click -> {
      if(cancelButtonClick != null){
        cancelButtonClick.handle(click);
      }
      close();
    });
  }

  public void showWindow(String title, String message, EventHandler acceptButtonClick, EventHandler cancelButtonClick){
    System.out.println("Warning popup show window!");
    this.message.setText(message);
    this.acceptButtonClick = acceptButtonClick;
    this.cancelButtonClick = cancelButtonClick;
    cancelButton.setVisible(true);

    setTitle(title);
    setScene(scene);
    show();
  }

  public void showWindow(String title, String message, EventHandler acceptButtonClick){
    cancelButton.setVisible(false);
    this.message.setText(message);
    this.acceptButtonClick = acceptButtonClick;

    setTitle(title);
    setScene(scene);
    show();
  }

  public void showWindow(Result result){
    cancelButton.setVisible(false);
    message.setText(result.getMessage());

    setScene(scene);
    show();
  }
}
