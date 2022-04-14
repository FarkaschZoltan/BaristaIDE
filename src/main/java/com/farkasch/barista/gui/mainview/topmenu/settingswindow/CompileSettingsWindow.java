package com.farkasch.barista.gui.mainview.topmenu.settingswindow;

import com.farkasch.barista.services.FileService;
import com.farkasch.barista.services.PersistenceService;
import com.farkasch.barista.util.settings.AbstractSetting;
import com.farkasch.barista.util.settings.JarSetting;
import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CompileSettingsWindow extends AbstractSettingsWindow {

  @Autowired
  private PersistenceService persistenceService;

  @Autowired
  private FileService fileService;

  @Override
  protected void initContent() {
    content = new Pane();
  }

  @Override
  protected void initSideMenu() {
    sideMenu = new VBox();

    Button addJars = new Button();
    addJars.setText("Add jar dependencies");
    addJars.setOnMouseClicked(click -> {
      content = addJarsPane();
      windowLayout.setCenter(content);
    });

    sideMenu.getChildren().addAll(addJars);
  }

  @Override
  protected void initScene() {
    setTitle("Compile Settings");
    scene = new Scene(windowLayout, 300, 400);
    scene.getStylesheets().add(
      Paths.get("src/main/java/com/farkasch/barista/style.css").toAbsolutePath().toUri()
        .toString());
    content = addJarsPane();
  }

  @Override
  protected void save(){
    for(AbstractSetting setting : settingList){
      if(setting.getClass().equals(JarSetting.class)){
        fileService.updateJarsInJarJson(((JarSetting) setting).getFile(), ((JarSetting) setting).getJars());
      }
    }

    settingList.clear();
  }

  @Override
  protected void cancel(){
    close();
  }

  private Pane addJarsPane(){
    VBox mainPane = new VBox();
    HBox searchBox = new HBox();
    ComboBox<File> mainFileSelector = new ComboBox<>();
    Button browseButton = new Button();
    Button addButton = new Button();
    ScrollPane jarsPane = new ScrollPane();
    GridPane jarSelector = new GridPane();

    AtomicInteger jarsCount = new AtomicInteger();

    mainFileSelector.setItems(FXCollections.observableList(persistenceService.updateAndGetCurrentMainFiles()));
    if(mainFileSelector.getItems().size() != 0){
      mainFileSelector.setValue(mainFileSelector.getItems().get(0));
      fillJarSelector(mainFileSelector.getValue().getAbsolutePath(), jarsCount, jarSelector);
    }
    mainFileSelector.setOnAction(event -> {
      jarsCount.set(0);
      fillJarSelector(mainFileSelector.getValue().getAbsolutePath(), jarsCount, jarSelector);
    });

    addButton.setText("Add");

    browseButton.setText("Browse");
    browseButton.setOnMouseClicked(click -> {
      FileChooser fileChooser = new FileChooser();
      fileChooser.setTitle("Browse Jars");
      fileChooser.getExtensionFilters().addAll(
        new ExtensionFilter("JAR", "*.jar")
      );
      List<File> chosenJars = fileChooser.showOpenMultipleDialog(this);
      if(chosenJars != null){
        List<String> fileNames = chosenJars.stream().map(File::getAbsolutePath).collect(Collectors.toList());
        settingList.add(new JarSetting(mainFileSelector.getValue().getAbsolutePath(), fileNames));
        for(File jar : chosenJars){
          Label jarLabel = new Label(jar.getName());
          jarSelector.add(jarLabel, 0, jarsCount.get());
          jarsCount.getAndIncrement();
        }
      }
    });

    searchBox.getChildren().addAll(mainFileSelector, addButton);
    jarsPane.setContent(jarSelector);
    mainPane.getChildren().addAll(searchBox, jarsPane, browseButton);

    return mainPane;
  }

  private void fillJarSelector(String mainFile, AtomicInteger jarsCount, GridPane jarSelector){
    List<String> fileData = fileService.getJarsForFile(mainFile);
    if(fileData.size() != 0){
      fileData.stream().forEach(jar -> {
        String splitJar[] = jar.split("\\\\");
        Label jarLabel = new Label(splitJar[splitJar.length - 1]);
        jarSelector.add(jarLabel, 0, jarsCount.get());
        jarsCount.getAndIncrement();
      });
    }
  }

}
