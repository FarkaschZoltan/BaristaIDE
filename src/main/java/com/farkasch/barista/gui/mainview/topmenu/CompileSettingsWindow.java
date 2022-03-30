package com.farkasch.barista.gui.mainview.topmenu;

import com.farkasch.barista.services.FileService;
import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

@Component
public class CompileSettingsWindow extends SettingsWindow {

  @Override
  protected void initContent() {
    content = new Pane();
  }

  @Override
  protected void initSideMenu() {
    sideMenu = new VBox();

    Button addJars = new Button();
    addJars.setOnMouseClicked(click -> {
      //content = addJarsPane();
      windowLayout.setCenter(content);
    });
  }

  @Override
  protected void initScene() {
    setTitle("Compile Settings");
    scene = new Scene(windowLayout, 300, 400);
    scene.getStylesheets().add(
      Paths.get("src/main/java/com/farkasch/barista/style.css").toAbsolutePath().toUri()
        .toString());
  }

  private void addJarsPane(){
    VBox mainPane = new VBox();
    HBox searchBox = new HBox();
    TextField searchField = new TextField();
    Button browseButton = new Button();
    Button addButton = new Button();
    ScrollPane jarsPane = new ScrollPane();
    GridPane jarSelector = new GridPane();

    browseButton.setOnMouseClicked(click -> {
      FileChooser fileChooser = new FileChooser();
      fileChooser.setTitle("Browse Jars");
      fileChooser.getExtensionFilters().addAll(
        new ExtensionFilter("JAR", "*.jar")
      );
      List<File> chosenJars = fileChooser.showOpenMultipleDialog(this);
      //FileService.updateJarsJarJson();
      for(File jar : chosenJars){
        Label jarLabel = new Label(jar.getName());
      }
    });
  }

}
