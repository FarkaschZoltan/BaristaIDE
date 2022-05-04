package com.farkasch.barista.gui.component;

import java.nio.file.Paths;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javax.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class WarningPopup extends Stage {

  protected Text message;
  protected Button acceptButton;
  protected Button cancelButton;
  protected BorderPane windowLayout;
  protected HBox textLayout;
  protected GridPane buttonLayout;

  protected Scene scene;
  protected EventHandler<ActionEvent> acceptButtonClick;
  protected EventHandler<ActionEvent> cancelButtonClick;

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

    windowLayout.setCenter(textLayout);
    windowLayout.setBottom(buttonLayout);

    buttonLayout.addColumn(0, cancelButton);
    buttonLayout.addColumn(1, acceptButton);
    buttonLayout.setHgap(10);
    BorderPane.setMargin(buttonLayout, new Insets(0, 0, 10, 0));

    windowLayout.setMinHeight(scene.getHeight());
    windowLayout.setMinWidth(scene.getWidth());
    windowLayout.setMaxWidth(Double.MAX_VALUE);
    windowLayout.setMaxHeight(Double.MAX_VALUE);
    textLayout.setAlignment(Pos.CENTER);
    buttonLayout.setAlignment(Pos.BOTTOM_CENTER);

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
    this.message.setText(message);
    this.acceptButtonClick = acceptButtonClick;
    this.cancelButtonClick = cancelButtonClick;
    setTitle(title);
    setScene(scene);
    show();
  }
}
