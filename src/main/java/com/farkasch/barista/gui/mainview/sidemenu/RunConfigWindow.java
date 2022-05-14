package com.farkasch.barista.gui.mainview.sidemenu;

import java.nio.file.Paths;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javax.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class RunConfigWindow extends Stage {

  //Frame design
  private Button addDependencies;
  private Button addRunConfig;
  private Button editRunConfig;
  private HBox menuBar;
  private BorderPane windowLayout;
  private Scene scene;

  //Dependency design
  private GridPane dependencyLayout;
  private GridPane browseLayout;
  private ScrollPane jarScrollPane;
  private GridPane jarSelector;
  private Label jarSelectorLabel;
  private Button browseButton;

  //new Run-config design
  private GridPane addRunConfigLayout;

  //edit Run-config design
  private GridPane editRunConfigLayout;

  @PostConstruct
  private void init(){
    initFrame();
    initDependency();
    initNewRunConfig();
    initNewRunConfig();
  }

  private void initFrame(){
    addDependencies = new Button("Add Dependencies");
    addRunConfig = new Button("Add Run-Configuration");
    editRunConfig = new Button("Edit Run-Configuration");
    menuBar = new HBox(addDependencies, addRunConfig, editRunConfig);
    windowLayout = new BorderPane();

    scene = new Scene(windowLayout, 300, 400);
    scene.getStylesheets().add(
      Paths.get("src/main/java/com/farkasch/barista/style.css").toAbsolutePath().toUri().toString());

    addDependencies.setOnAction(event -> {
      windowLayout.setCenter(dependencyLayout);
    });
    addDependencies.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(addDependencies, Priority.ALWAYS);

    addRunConfig.setOnAction(event -> {
      windowLayout.setCenter(addRunConfigLayout);
    });
    addRunConfig.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(addRunConfig, Priority.ALWAYS);

    editRunConfig.setOnAction(event -> {
      windowLayout.setCenter(editRunConfigLayout);
    });
    editRunConfig.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(editRunConfig, Priority.ALWAYS);

    windowLayout.setTop(menuBar);

    initModality(Modality.APPLICATION_MODAL);
    setResizable(false);
  }

  private void initDependency(){
    browseButton = new Button("Browse...");
    jarSelectorLabel = new Label("Selected jar dependencies: ");
    browseLayout = new GridPane();
    dependencyLayout = new GridPane();
    jarSelector = new GridPane();
    jarScrollPane = new ScrollPane();

    jarSelectorLabel.setLabelFor(jarSelector);

    dependencyLayout.add(jarSelectorLabel, 0, 0);
    dependencyLayout.add(jarSelector, 1, 0);
    dependencyLayout.add(browseButton, 2, 0);

  }

  private void initNewRunConfig(){

  }

  private void initEditRunConfig(){

  }

  private void onLoad(){

  }

  public void showWindow(){
    onLoad();
    setScene(scene);
    show();
  }
}
