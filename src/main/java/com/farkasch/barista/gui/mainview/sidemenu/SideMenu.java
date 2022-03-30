package com.farkasch.barista.gui.mainview.sidemenu;

import com.farkasch.barista.JavaFxApp;
import com.farkasch.barista.services.PersistenceService;
import com.farkasch.barista.services.ProcessService;
import java.io.File;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javax.annotation.PostConstruct;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class SideMenu extends BorderPane {

  @Lazy
  @Autowired
  private JavaFxApp javaFxApp;
  @Autowired
  private ProcessService processService;
  @Autowired
  private PersistenceService persistenceService;

  private HBox topMenu;
  private VBox content;
  private Button compileButton;
  private Button runButton;

  @PostConstruct
  private void init() {
    setId("side-menu");
    initTopMenu();
    initContent();
  }

  private void initTopMenu() {
    topMenu = new HBox();
    initCompileButton();
    initRunButton();

    topMenu.getChildren().addAll(compileButton, runButton);

    setTop(topMenu);
    setCenter(content);
  }

  private void initContent() {
    content = new VBox();
    content.setId("side-menu__content");
  }

  private void initCompileButton() {
    compileButton = new Button("Compile");
    compileButton.setGraphic(new FontIcon("mdi-wrench"));
    compileButton.setOnMouseClicked(click -> {
      File f = persistenceService.getActiveInterface().getShownFile();
      String filePath = f.getParentFile().getPath();
      String fileName = f.getName();
      System.out.println(filePath + " - " + fileName);
      processService.Compile(filePath, fileName, null);
    });
  }

  private void initRunButton() {
    runButton = new Button("Run");
    runButton.setGraphic(new FontIcon("mdi-play"));
    runButton.setOnMouseClicked(click -> {
      File f = persistenceService.getActiveInterface().getShownFile();
      String filePath = f.getParentFile().getPath();
      String fileName = f.getName();
      processService.Run(filePath, fileName, null);
    });
  }
}
