package com.farkasch.barista.gui.mainview.sidemenu;

import com.farkasch.barista.services.FileService;
import com.farkasch.barista.services.PersistenceService;
import com.farkasch.barista.util.settings.JarSetting;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javax.annotation.PostConstruct;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RunConfigWindow extends Stage {

  @Autowired
  private PersistenceService persistenceService;
  @Autowired
  private FileService fileService;

  //Frame design
  private Button addDependencies;
  private Button addRunConfig;
  private Button editRunConfig;
  private HBox menuBar;
  private BorderPane windowLayout;
  private Scene scene;

  private File mainFile;

  //Dependency design
  private GridPane dependencyLayout;
  private GridPane browseLayout;
  private ScrollPane jarScrollPane;
  private GridPane jarSelector;
  private Label jarSelectorLabel;
  private Button browseButton;

  private List<JarSetting> jarSettings;

  //new Run-config design
  private GridPane addRunConfigLayout;

  //edit Run-config design
  private GridPane editRunConfigLayout;

  @PostConstruct
  private void init() {
    initFrame();
    initDependency();
    initNewRunConfig();
    initNewRunConfig();
  }

  private void initFrame() {
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

  private void initDependency() {
    browseButton = new Button("Browse...");
    jarSelectorLabel = new Label("Selected jar dependencies: ");
    browseLayout = new GridPane();
    dependencyLayout = new GridPane();
    jarSelector = new GridPane();
    jarScrollPane = new ScrollPane();

    jarSettings = new ArrayList<>();

    jarSelectorLabel.setLabelFor(jarSelector);

    browseButton.setOnAction(event -> {
      FileChooser fileChooser = new FileChooser();
      fileChooser.setTitle("Browse Jars");
      fileChooser.getExtensionFilters().addAll(
        new ExtensionFilter("JAR", "*.jar")
      );
      List<File> chosenJars = fileChooser.showOpenMultipleDialog(this);
      if (chosenJars != null) {
        List<String> fileNames = chosenJars.stream().map(File::getAbsolutePath).collect(Collectors.toList());
        fileNames.stream().forEach(fileName -> jarSelector.add(new JarItem(new File(fileName).getName(), fileName), 0, jarSelector.getRowCount()));
      }
    });

    dependencyLayout.add(jarSelectorLabel, 0, 0);
    dependencyLayout.add(jarSelector, 1, 0);
    dependencyLayout.add(browseButton, 2, 0);

  }

  private void initNewRunConfig() {

  }

  private void initEditRunConfig() {

  }

  private void onLoad(File mainFile) {
    if (persistenceService.getOpenProject() != null) {

    } else {
      this.mainFile = mainFile;
      fileService.getJarsForFile(mainFile.getAbsolutePath()).stream()
        .forEach(jar -> jarSelector.add(new JarItem(new File(jar).getName(), jar), 0, jarSelector.getRowCount()));
    }

  }

  public void showWindow(File mainFile) {
    onLoad(mainFile);
    setScene(scene);
    show();
  }

  private class JarItem extends HBox {

    private Label jarNameLabel;
    private Region separator;
    private Button removeButton;

    private String jarName;
    private String jarPath;

    public JarItem(String jarName, String jarPath) {
      this.jarName = jarName;
      this.jarPath = jarPath;

      jarNameLabel = new Label(jarName);
      separator = new Region();
      removeButton = new Button();

      HBox.setHgrow(separator, Priority.ALWAYS);

      removeButton.setGraphic(new FontIcon("mdi-minus"));
      removeButton.setGraphicTextGap(0);
      removeButton.setTextAlignment(TextAlignment.CENTER);
      AtomicBoolean found = new AtomicBoolean(false);
      removeButton.setOnAction(event -> {
        for (Node jar : jarSelector.getChildren()) {
          if (found.get()) {
            GridPane.setRowIndex(jar, jarSelector.getChildren().indexOf(jar) - 1);
          }
          if (jar.equals(this)) {
            found.set(true);
          }
        }
        jarSelector.getChildren().remove(this);
      });

      getChildren().addAll(jarNameLabel, separator, removeButton);
    }

    public String getJarName() {
      return jarName;
    }

    public void setJarName(String jarName) {
      this.jarName = jarName;
    }

    public String getJarPath() {
      return jarPath;
    }

    public void setJarPath(String jarPath) {
      this.jarPath = jarPath;
    }
  }
}
