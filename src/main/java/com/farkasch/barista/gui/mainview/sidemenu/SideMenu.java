package com.farkasch.barista.gui.mainview.sidemenu;

import com.farkasch.barista.MainApp;
import com.farkasch.barista.services.ProcessService;
import java.io.File;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

public class SideMenu extends BorderPane {

  private HBox topMenu;
  private VBox content;
  private Button compileButton;
  private Button runButton;

  private MainApp mainApp;

  public SideMenu(MainApp mainApp){

    this.mainApp = mainApp;

    setId("side-menu");
    initTopMenu();
    initContent();
  }

  private void initTopMenu(){
    topMenu = new HBox();
    initCompileButton();
    initRunButton();

    topMenu.getChildren().addAll(compileButton, runButton);

    setTop(topMenu);
    setCenter(content);
  }

  private void initContent(){

    content = new VBox();
    content.setId("side-menu__content");
  }

  private void initCompileButton(){
    compileButton = new Button("Compile");
    compileButton.setGraphic(new FontIcon("mdi-wrench"));
    compileButton.setOnMouseClicked(click -> {
      File f = mainApp.getCodeArea().getActiveInterface().getShownFile();
      String filePath = f.getParentFile().getPath();
      String fileName = f.getName();
      System.out.println(filePath + " - " + fileName);
      ProcessService.Compile(filePath, fileName, null);
    });
  }

  private void initRunButton(){
    runButton = new Button("Run");
    runButton.setGraphic(new FontIcon("mdi-play"));
    runButton.setOnMouseClicked(click -> {
      File f = mainApp.getCodeArea().getActiveInterface().getShownFile();
      String filePath = f.getParentFile().getPath();
      String fileName = f.getName();
      ProcessService.Run(filePath, fileName, null);
    });
  }
}
