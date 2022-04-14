package com.farkasch.barista.gui.mainview.sidemenu;

import com.farkasch.barista.JavaFxApp;
import com.farkasch.barista.services.FileService;
import com.farkasch.barista.services.PersistenceService;
import com.farkasch.barista.services.ProcessService;
import com.farkasch.barista.util.enums.JavacEnum;
import java.io.File;
import java.util.HashMap;
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

  @Autowired
  private ProcessService processService;
  @Autowired
  private PersistenceService persistenceService;
  @Autowired
  private FileService fileService;

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
      File f = persistenceService.getMainFiles().get(0);
      String filePath = f.getParentFile().getPath();
      String fileName = f.getName();
      //TODO: main file selector
      processService.Compile(filePath, fileName);
    });
  }

  private void initRunButton() {
    runButton = new Button("Run");
    runButton.setGraphic(new FontIcon("mdi-play"));
    runButton.setOnMouseClicked(click -> {
      File f = persistenceService.getMainFiles().get(0);
      String filePath = f.getParentFile().getPath();
      String fileName = f.getName();
      processService.Run(filePath, fileName);
    });
  }
}
