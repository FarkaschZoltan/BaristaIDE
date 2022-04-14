package com.farkasch.barista.gui.mainview.topmenu.settingswindow;

import com.farkasch.barista.util.settings.AbstractSetting;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

@Component
public abstract class AbstractSettingsWindow extends Stage {

  //Design
  protected BorderPane windowLayout;
  protected VBox sideMenu;
  protected Pane content;
  protected Scene scene;

  private HBox buttonLayout;
  private Button confirmButton;
  private Button cancelButton;
  private Button applyButton;

  protected List<AbstractSetting> settingList;

  public void showWindow(){
    windowLayout = new BorderPane();
    settingList = new ArrayList<>();

    initContent();
    initSideMenu();
    initButtonLayout();
    initScene();

    windowLayout.setCenter(content);
    windowLayout.setLeft(sideMenu);
    windowLayout.setBottom(buttonLayout);

    setScene(scene);
    show();
  }

  protected abstract void initContent();

  protected abstract void initSideMenu();

  protected abstract void initScene();

  private void initButtonLayout(){
    buttonLayout = new HBox();
    confirmButton = new Button();
    applyButton = new Button();
    cancelButton = new Button();

    applyButton.setAlignment(Pos.CENTER_RIGHT);
    applyButton.setText("Apply");
    applyButton.setOnAction(click -> save());
    confirmButton.setAlignment(Pos.CENTER_RIGHT);
    confirmButton.setText("OK");
    confirmButton.setOnAction(click -> {
      save();
      close();
    });
    cancelButton.setAlignment(Pos.CENTER_RIGHT);
    cancelButton.setText("CANCEL");
    cancelButton.setOnAction(click -> cancel());

    buttonLayout.getChildren().addAll(applyButton, confirmButton, cancelButton);
  }

  protected abstract void save();

  protected abstract void cancel();

}
