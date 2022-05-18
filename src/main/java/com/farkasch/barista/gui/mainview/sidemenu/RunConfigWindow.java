package com.farkasch.barista.gui.mainview.sidemenu;

import com.farkasch.barista.gui.component.WarningPopup;
import com.farkasch.barista.services.FileService;
import com.farkasch.barista.services.PersistenceService;
import com.farkasch.barista.util.settings.RunSetting;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
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
import javafx.util.StringConverter;
import javax.annotation.PostConstruct;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class RunConfigWindow extends Stage {

  @Autowired
  private PersistenceService persistenceService;
  @Autowired
  private FileService fileService;
  @Autowired
  private WarningPopup warningPopup;
  @Lazy
  @Autowired
  private SideMenu sideMenu;

  //Frame design
  private Button addDependencies;
  private Button addRunConfig;
  private Button editRunConfig;
  private Button acceptButton;
  private Button applyButton;
  private Button cancelButton;
  private HBox menuBar;
  private BorderPane windowLayout;
  private HBox buttonLayout;
  private Region buttonLayoutRegion;
  private Scene scene;

  private File mainFile;

  //Dependency design
  private GridPane dependencyLayout;
  private GridPane browseLayout;
  private ScrollPane jarScrollPane;
  private GridPane jarSelector;
  private Label jarSelectorLabel;
  private Button browseButton;


  //new Run-config design
  private GridPane addRunConfigLayout;
  private Label newRunConfigLabel;
  private Label newRunConfigNameLabel;
  private TextField newRunConfigTextField;
  private TextField newRunConfigNameTextField;
  private Button addNewRunConfigButton;

  //edit Run-config design
  private GridPane editRunConfigLayout;
  private ComboBox<RunSetting> runSettingsComboBox;
  private Label editRunConfigLabel;
  private Label editRunConfigNameLabel;
  private Label runSettingsComboBoxLabel;
  private TextField editRunConfigTextField;
  private TextField editRunConfigNameTextField;
  private Button editRunConfigButton;
  private Button deleteRunConfigButton;
  private Region editButtonLayoutRegion;
  private HBox editButtonLayout;
  private String oldName;
  private ObservableList<RunSetting> runSettingList;

  @PostConstruct
  private void init() {
    initFrame();
    initDependency();
    initNewRunConfig();
    initEditRunConfig();
  }

  private void initFrame() {
    addDependencies = new Button("Add Dependencies");
    addRunConfig = new Button("Add Run-Configuration");
    editRunConfig = new Button("Edit Run-Configuration");
    acceptButton = new Button("OK");
    cancelButton = new Button("Cancel");
    applyButton = new Button("Apply");
    menuBar = new HBox(addDependencies, addRunConfig, editRunConfig);
    windowLayout = new BorderPane();
    buttonLayout = new HBox();
    buttonLayoutRegion = new Region();

    scene = new Scene(windowLayout, 500, 400);
    scene.getStylesheets().add(
      Paths.get("src/main/java/com/farkasch/barista/style.css").toAbsolutePath().toUri().toString());

    addDependencies.setOnAction(event -> {
      addDependencies.setId("switch-menu__item--selected");
      addRunConfig.setId("switch-menu__item");
      editRunConfig.setId("switch-menu__item");
      windowLayout.setCenter(dependencyLayout);
    });
    addDependencies.setId("switch-menu__item");
    addDependencies.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(addDependencies, Priority.ALWAYS);
    addRunConfig.setOnAction(event -> {
      addRunConfig.setId("switch-menu__item--selected");
      addDependencies.setId("switch-menu__item");
      editRunConfig.setId("switch-menu__item");
      windowLayout.setCenter(addRunConfigLayout);
    });
    addRunConfig.setMaxWidth(Double.MAX_VALUE);
    addRunConfig.setId("switch-menu__item");
    HBox.setHgrow(addRunConfig, Priority.ALWAYS);
    editRunConfig.setOnAction(event -> {
      editRunConfig.setId("switch-menu__item--selected");
      addRunConfig.setId("switch-menu__item");
      addDependencies.setId("switch-menu__item");
      windowLayout.setCenter(editRunConfigLayout);
    });
    editRunConfig.setId("switch-menu__item");
    editRunConfig.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(editRunConfig, Priority.ALWAYS);

    acceptButton.setOnAction(event -> {
      save();
      close();
    });
    HBox.setMargin(acceptButton, new Insets(20, 0, 10, 0));
    cancelButton.setOnAction(event -> {
      close();
    });
    HBox.setMargin(cancelButton, new Insets(20, 0, 10, 10));
    applyButton.setOnAction(event -> {
      save();
    });
    HBox.setMargin(applyButton, new Insets(20, 10, 10, 10));
    HBox.setHgrow(buttonLayoutRegion, Priority.ALWAYS);
    buttonLayout.getChildren().addAll(buttonLayoutRegion, acceptButton, cancelButton, applyButton);

    windowLayout.setTop(menuBar);
    windowLayout.setBottom(buttonLayout);

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

    jarSelectorLabel.setLabelFor(jarSelector);
    GridPane.setHgrow(jarSelectorLabel, Priority.ALWAYS);

    browseButton.setOnAction(event -> {
      FileChooser fileChooser = new FileChooser();
      fileChooser.setTitle("Browse Jars");
      fileChooser.getExtensionFilters().addAll(
        new ExtensionFilter("JAR", "*.jar")
      );
      List<File> chosenJars = fileChooser.showOpenMultipleDialog(this);
      if (chosenJars != null) {
        List<String> fileNames = chosenJars.stream().map(File::getAbsolutePath).collect(Collectors.toList());
        fileNames.stream().forEach(fileName -> {
          JarItem jarItem = new JarItem(new File(fileName).getName(), fileName);
          GridPane.setHgrow(jarItem, Priority.ALWAYS);
          GridPane.setFillWidth(jarItem, true);
          jarSelector.add(jarItem, 0, jarSelector.getRowCount());
        });
      }
    });
    GridPane.setHgrow(browseButton, Priority.ALWAYS);
    GridPane.setHalignment(browseButton, HPos.RIGHT);

    jarSelector.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
    jarSelector.minWidthProperty().bind(jarScrollPane.widthProperty());

    jarScrollPane.setContent(jarSelector);
    GridPane.setMargin(jarScrollPane, new Insets(5, 0, 10, 0));
    GridPane.setHgrow(jarScrollPane, Priority.ALWAYS);
    GridPane.setVgrow(jarScrollPane, Priority.ALWAYS);

    dependencyLayout.setPadding(new Insets(10));
    dependencyLayout.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
    dependencyLayout.prefHeightProperty().bind(scene.heightProperty());
    dependencyLayout.prefWidthProperty().bind(scene.widthProperty());
    dependencyLayout.add(jarSelectorLabel, 0, 0);
    dependencyLayout.add(jarScrollPane, 0, 1);
    dependencyLayout.add(browseButton, 0, 2);
  }

  private void initNewRunConfig() {
    addRunConfigLayout = new GridPane();
    newRunConfigLabel = new Label("Command: ");
    newRunConfigNameLabel = new Label("Name: ");
    newRunConfigTextField = new TextField();
    newRunConfigNameTextField = new TextField();
    addNewRunConfigButton = new Button("Add");

    newRunConfigLabel.setLabelFor(newRunConfigTextField);
    newRunConfigNameLabel.setLabelFor(newRunConfigNameTextField);

    newRunConfigTextField.setMaxWidth(Double.MAX_VALUE);
    GridPane.setHgrow(newRunConfigTextField, Priority.ALWAYS);
    GridPane.setFillWidth(newRunConfigTextField, true);
    GridPane.setMargin(newRunConfigTextField, new Insets(5, 0, 10, 0));

    newRunConfigNameTextField.setMaxWidth(Double.MAX_VALUE);
    GridPane.setHgrow(newRunConfigNameTextField, Priority.ALWAYS);
    GridPane.setFillWidth(newRunConfigNameTextField, true);
    GridPane.setMargin(newRunConfigNameTextField, new Insets(5, 0, 10, 0));

    addNewRunConfigButton.setOnAction(event -> {
      if (!runSettingList.stream().map(RunSetting::getName).toList().contains(newRunConfigNameTextField.getText())
        && !newRunConfigNameTextField.getText().equals(persistenceService.getOpenProject().getProjectName())) {
        runSettingList.add(new RunSetting(newRunConfigNameTextField.getText(), newRunConfigTextField.getText()));
        runSettingList.stream().forEach(System.out::println);
        runSettingsComboBox.setItems(runSettingList);
      } else {
        warningPopup.showWindow("Error", "A run-config with this name already exists!", null);
      }
    });
    GridPane.setHgrow(addNewRunConfigButton, Priority.ALWAYS);
    GridPane.setHalignment(addNewRunConfigButton, HPos.RIGHT);

    addRunConfigLayout.setPadding(new Insets(10));
    addRunConfigLayout.setAlignment(Pos.TOP_CENTER);
    addRunConfigLayout.add(newRunConfigNameLabel, 0, 0);
    addRunConfigLayout.add(newRunConfigNameTextField, 0, 1);
    addRunConfigLayout.add(newRunConfigLabel, 0, 2);
    addRunConfigLayout.add(newRunConfigTextField, 0, 3);
    addRunConfigLayout.add(addNewRunConfigButton, 0, 4);
  }

  private void initEditRunConfig() {
    editRunConfigLayout = new GridPane();
    runSettingsComboBox = new ComboBox<>();
    editRunConfigNameLabel = new Label("Name: ");
    editRunConfigLabel = new Label("Command: ");
    runSettingsComboBoxLabel = new Label("Choose run config: ");
    editRunConfigTextField = new TextField();
    editRunConfigNameTextField = new TextField();
    editRunConfigButton = new Button("Save");
    deleteRunConfigButton = new Button("Delete");
    editButtonLayoutRegion = new Region();
    editButtonLayout = new HBox(editButtonLayoutRegion, deleteRunConfigButton, editRunConfigButton);
    runSettingList = FXCollections.observableArrayList();

    editRunConfigLabel.setLabelFor(editRunConfigTextField);
    editRunConfigNameLabel.setLabelFor(editRunConfigNameTextField);
    runSettingsComboBoxLabel.setLabelFor(runSettingsComboBox);

    runSettingsComboBox.setItems(runSettingList);
    runSettingsComboBox.setMaxWidth(Double.MAX_VALUE);
    runSettingsComboBox.setOnAction(event -> {
      if(runSettingsComboBox.getValue() != null){
        editRunConfigNameTextField.setText(runSettingsComboBox.getValue().getName());
        oldName = runSettingsComboBox.getValue().getName();
        editRunConfigTextField.setText(runSettingsComboBox.getValue().getCommand());
      } else {
        editRunConfigNameTextField.setText("");
        oldName = "";
        editRunConfigTextField.setText("");
      }
    });
    runSettingsComboBox.setConverter(new StringConverter<>() {
      @Override
      public String toString(RunSetting runSetting) {
        return runSetting == null ? "" : runSetting.getName();
      }

      @Override
      public RunSetting fromString(String s) {
        for (RunSetting runSetting : runSettingsComboBox.getItems()) {
          if (runSetting.getName().equals(s)) {
            return runSetting;
          }
        }
        return null;
      }
    });
    GridPane.setHgrow(runSettingsComboBox, Priority.ALWAYS);
    GridPane.setFillWidth(runSettingsComboBox, true);
    GridPane.setMargin(runSettingsComboBox, new Insets(5, 0, 10, 0));

    editRunConfigTextField.setMaxWidth(Double.MAX_VALUE);
    GridPane.setHgrow(editRunConfigTextField, Priority.ALWAYS);
    GridPane.setFillWidth(editRunConfigTextField, true);
    GridPane.setMargin(editRunConfigTextField, new Insets(5, 0, 10, 0));

    editRunConfigNameTextField.setMaxWidth(Double.MAX_VALUE);
    GridPane.setHgrow(editRunConfigNameTextField, Priority.ALWAYS);
    GridPane.setFillWidth(editRunConfigNameTextField, true);
    GridPane.setMargin(editRunConfigNameTextField, new Insets(5, 0, 10, 0));

    editRunConfigButton.setOnAction(event -> {
      RunSetting editedSetting;
      if (oldName.equals(editRunConfigNameTextField.getText())) {
        editedSetting = new RunSetting(oldName, editRunConfigTextField.getText());
        runSettingList.remove(editedSetting);
        runSettingList.add(editedSetting);
        runSettingsComboBox.setValue(editedSetting);
      } else {
        if (!runSettingList.stream().map(RunSetting::getName).toList().contains(editRunConfigNameTextField.getText())
          && !editRunConfigNameTextField.getText().equals(persistenceService.getOpenProject().getProjectName())) {
          editedSetting = new RunSetting(editRunConfigNameTextField.getText(), editRunConfigTextField.getText());
          runSettingList.remove(new RunSetting(oldName, null));
          runSettingList.add(editedSetting);
          runSettingsComboBox.setValue(editedSetting);
          oldName = editRunConfigNameTextField.getText();
        } else {
          warningPopup.showWindow("Error", "A run-config with this name already exists!", null);
        }
      }

    });
    HBox.setMargin(editRunConfigButton, new Insets(0, 0, 0, 10));

    deleteRunConfigButton.setOnAction(event -> {
      if(runSettingsComboBox.getValue() != null){
        runSettingList.remove(runSettingsComboBox.getValue());
      }
    });

    HBox.setHgrow(editButtonLayoutRegion, Priority.ALWAYS);

    editRunConfigLayout.setPadding(new Insets(10));
    editRunConfigLayout.setAlignment(Pos.TOP_CENTER);
    editRunConfigLayout.add(runSettingsComboBoxLabel, 0, 0);
    editRunConfigLayout.add(runSettingsComboBox, 0, 1);
    editRunConfigLayout.add(editRunConfigNameLabel, 0, 2);
    editRunConfigLayout.add(editRunConfigNameTextField, 0, 3);
    editRunConfigLayout.add(editRunConfigLabel, 0, 4);
    editRunConfigLayout.add(editRunConfigTextField, 0, 5);
    editRunConfigLayout.add(editButtonLayout, 0, 6);
  }

  private void save() {
    List<String> jars = jarSelector.getChildren().stream().map(jarItem -> ((JarItem) jarItem).jarPath).toList();
    if (persistenceService.getOpenProject() != null) {
      persistenceService.getOpenProject().setJars(new ArrayList<>(jars));
      fileService.setRunConfig(runSettingsComboBox.getItems());
      sideMenu.getRunSettingsComboBox().setItems(FXCollections.observableList(fileService.getRunConfig()));
      fileService.saveProject();
    } else {
      fileService.updateJarsInJarConfig(mainFile.getAbsolutePath(), jars);
    }
  }

  private void onLoad(File mainFile) {
    jarSelector.getChildren().clear();
    addDependencies.getOnAction().handle(new ActionEvent());

    if (persistenceService.getOpenProject() != null) {
      addRunConfig.setDisable(false);
      editRunConfig.setDisable(false);
      runSettingList = (FXCollections.observableArrayList(
        fileService.getRunConfig().stream().filter(sett -> !sett.getName().equals(persistenceService.getOpenProject().getProjectName())).toList()));
      runSettingsComboBox.setItems(runSettingList);
      runSettingsComboBox.setValue(null);
      newRunConfigNameTextField.setText("");
      newRunConfigTextField.setText("");
      editRunConfigNameTextField.setText("");
      editRunConfigTextField.setText("");
      persistenceService.getOpenProject().getJars().stream().forEach(fileName -> {
        JarItem jarItem = new JarItem(new File(fileName).getName(), fileName);
        GridPane.setHgrow(jarItem, Priority.ALWAYS);
        GridPane.setFillWidth(jarItem, true);
        jarSelector.add(jarItem, 0, jarSelector.getRowCount());
      });
    } else {
      this.mainFile = mainFile;
      fileService.getJarsForFile(mainFile.getAbsolutePath()).stream()
        .forEach(fileName -> {
          JarItem jarItem = new JarItem(new File(fileName).getName(), fileName);
          GridPane.setHgrow(jarItem, Priority.ALWAYS);
          GridPane.setFillWidth(jarItem, true);
          jarSelector.add(jarItem, 0, jarSelector.getRowCount());
        });
      addRunConfig.setDisable(true);
      editRunConfig.setDisable(true);
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
      setMaxWidth(Double.MAX_VALUE);
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
