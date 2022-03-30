package com.farkasch.barista.gui.mainview.topmenu;

import java.nio.file.Paths;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

@Component
public abstract class SettingsWindow extends Stage {

  //Design
  protected BorderPane windowLayout;
  protected VBox sideMenu;
  protected Pane content;
  protected Scene scene;

  public SettingsWindow() {
    windowLayout = new BorderPane();

    initContent();
    initSideMenu();
    initScene();

    windowLayout.setCenter(content);
    windowLayout.setLeft(sideMenu);
  }

  protected abstract void initContent();

  protected abstract void initSideMenu();

  protected abstract void initScene();

}
