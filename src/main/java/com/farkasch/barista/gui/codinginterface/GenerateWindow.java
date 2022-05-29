package com.farkasch.barista.gui.codinginterface;

import com.farkasch.barista.services.FileService;
import com.farkasch.barista.services.JavaScriptService;
import com.farkasch.barista.services.PersistenceService;
import com.farkasch.barista.util.FileTemplates;
import com.farkasch.barista.util.enums.GenerateEnum;
import com.google.common.io.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GenerateWindow extends Stage {

  @Autowired
  private FileService fileService;
  @Autowired
  private JavaScriptService javaScriptService;
  @Autowired
  private PersistenceService persistenceService;
  @Autowired
  private FileTemplates fileTemplates;

  //Design
  private Label generateComboBoxLabel;
  private Label classVariablesLabel;
  private ComboBox<GenerateEnum> generateComboBox;
  private Button generateButton;
  private Button cancelButton;
  private BorderPane windowLayout;
  private ScrollPane classVariablesScrollPane;
  private GridPane classVariables;
  private GridPane contentLayout;
  private GridPane topLayout;
  private HBox buttonLayout;
  private Region buttonLayoutRegion;
  private Scene scene;

  private ArrayList<ClassVariableItem> clickedItems;
  private int generateInsertPosition;
  private CodingInterface codingInterface;

  @PostConstruct
  private void init() {
    generateComboBoxLabel = new Label("Generate: ");
    classVariablesLabel = new Label("Class variables: ");
    generateComboBox = new ComboBox<>();
    generateButton = new Button("Generate");
    cancelButton = new Button("Cancel");
    windowLayout = new BorderPane();
    classVariablesScrollPane = new ScrollPane();
    classVariables = new GridPane();
    contentLayout = new GridPane();
    topLayout = new GridPane();
    buttonLayout = new HBox();
    buttonLayoutRegion = new Region();

    clickedItems = new ArrayList<>();

    setTitle("Generate...");
    scene = new Scene(windowLayout, 300, 400);
    scene.getStylesheets().add("style.css");

    generateComboBoxLabel.setLabelFor(generateComboBox);
    GridPane.setHgrow(generateComboBoxLabel, Priority.ALWAYS);
    GridPane.setHalignment(generateComboBoxLabel, HPos.LEFT);

    generateComboBox.setMaxWidth(Double.MAX_VALUE);
    generateComboBox.setItems(
      FXCollections.observableArrayList(List.of(GenerateEnum.CONSTRUCTOR, GenerateEnum.GETTER, GenerateEnum.SETTER, GenerateEnum.GETTER_AND_SETTER)));
    generateComboBox.setConverter(new StringConverter<>() {
      @Override
      public String toString(GenerateEnum generateEnum) {
        return generateEnum == null ? "" : generateEnum.getName();
      }

      @Override
      public GenerateEnum fromString(String s) {
        for (GenerateEnum generateEnum : generateComboBox.getItems()) {
          if (generateEnum.getName().equals(s)) {
            return generateEnum;
          }
        }
        return null;
      }
    });
    GridPane.setMargin(generateComboBox, new Insets(5, 0, 10, 0));
    GridPane.setHgrow(generateComboBox, Priority.ALWAYS);
    GridPane.setFillWidth(generateComboBox, true);

    topLayout.add(generateComboBoxLabel, 0, 0);
    topLayout.add(generateComboBox, 0, 1);

    classVariablesLabel.setLabelFor(classVariables);
    GridPane.setHgrow(classVariablesLabel, Priority.ALWAYS);
    GridPane.setHalignment(classVariablesLabel, HPos.LEFT);

    classVariables.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
    classVariables.minWidthProperty().bind(classVariablesScrollPane.widthProperty());
    classVariables.setGridLinesVisible(true);

    classVariablesScrollPane.setContent(classVariables);
    GridPane.setMargin(classVariablesScrollPane, new Insets(5, 0, 10, 0));
    GridPane.setHgrow(classVariablesScrollPane, Priority.ALWAYS);
    GridPane.setVgrow(classVariablesScrollPane, Priority.ALWAYS);

    contentLayout.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
    contentLayout.prefHeightProperty().bind(scene.heightProperty());
    contentLayout.prefWidthProperty().bind(scene.widthProperty());
    contentLayout.add(classVariablesLabel, 0, 0);
    contentLayout.add(classVariablesScrollPane, 0, 1);

    generateButton.setOnAction(event -> {
      generate();
      close();
    });
    HBox.setMargin(generateButton, new Insets(10, 0, 10, 10));

    cancelButton.setOnAction(event -> {
      close();
    });
    HBox.setMargin(cancelButton, new Insets(10, 0, 10, 0));

    HBox.setHgrow(buttonLayoutRegion, Priority.ALWAYS);
    buttonLayout.getChildren().addAll(buttonLayoutRegion, cancelButton, generateButton);

    windowLayout.setPadding(new Insets(10));
    windowLayout.setTop(topLayout);
    windowLayout.setCenter(contentLayout);
    windowLayout.setBottom(buttonLayout);
  }

  private void generate() {
    StringBuilder generatedContent = new StringBuilder();
    HashMap<String, String> variables = new HashMap<>();
    for (ClassVariableItem variableItem : clickedItems) {
      variables.put(variableItem.getVariableName(), variableItem.variableClass);
    }
    switch (generateComboBox.getValue()) {
      case CONSTRUCTOR:
        generatedContent.append("\n\n");
        generatedContent.append(
          fileTemplates.createConstructor(variables, Files.getNameWithoutExtension(codingInterface.getShownFile().getAbsolutePath())));
        break;
      case GETTER:
        generatedContent.append("\n\n");
        for (String name : variables.keySet()) {
          generatedContent.append(fileTemplates.createGetter(name, variables.get(name)));
        }
        break;
      case SETTER:
        generatedContent.append("\n\n");
        for(String name : variables.keySet()){
          generatedContent.append(fileTemplates.createSetter(name, variables.get(name)));
        }
        break;
      case GETTER_AND_SETTER:
        generatedContent.append("\n\n");
        for(String name : variables.keySet()){
          generatedContent.append(fileTemplates.createGetter(name, variables.get(name)));
          generatedContent.append(fileTemplates.createSetter(name, variables.get(name)));
        }
        break;
      default:
        break;
    }
    persistenceService.setGeneratedContent(generatedContent.toString());
    persistenceService.setGenerateInsertPosition(generateInsertPosition);
    javaScriptService.insertGeneratedContent(codingInterface.getContentWebView());
  }

  private void onLoad(CodingInterface codingInterface, int generateInsertPosition) {
    this.generateInsertPosition = generateInsertPosition;
    this.codingInterface = codingInterface;
    generateComboBox.setValue(GenerateEnum.CONSTRUCTOR);
    classVariables.getChildren().clear();
    List<String> classVariableList = fileService.getClassLevelVariables(javaScriptService.getContent(codingInterface.getContentWebView()));
    classVariableList.stream().forEach(variable -> {
      ClassVariableItem classVariableItem = new ClassVariableItem(variable);
      classVariables.add(classVariableItem, 0, classVariables.getRowCount());
    });
  }

  public void showWindow(CodingInterface codingInterface, int generateInsertPosition) {
    onLoad(codingInterface, generateInsertPosition);
    setScene(scene);
    show();
  }

  private class ClassVariableItem extends Button {

    private String variableName;
    private String variableClass;

    public ClassVariableItem(String variableName, String variableClass) {
      this.variableName = variableName;
      this.variableClass = variableClass;
      init();
    }

    public ClassVariableItem(String complexString) {
      String[] splitString = complexString.split("=");
      splitString = splitString[0].split(" ");
      variableName = splitString[splitString.length - 1];
      variableName = variableName.substring(0, variableName.length() - 1);
      variableClass = splitString[splitString.length - 2];
      init();
    }

    private void init() {
      setText(variableName + " : " + variableClass);
      setId("folder-dropdown__item");
      setOnAction(event -> {
        if (clickedItems.contains(this)) {
          clickedItems.remove(this);
          setId("folder-dropdown__item");
        } else {
          clickedItems.add(this);
          setId("folder-dropdown__item--selected");
        }
      });
      GridPane.setHgrow(this, Priority.ALWAYS);
      GridPane.setFillWidth(this, true);
      setMaxWidth(Double.MAX_VALUE);
    }

    public String getVariableName() {
      return variableName;
    }

    public void setVariableName(String variableName) {
      this.variableName = variableName;
    }

    public String getVariableClass() {
      return variableClass;
    }

    public void setVariableClass(String variableClass) {
      this.variableClass = variableClass;
    }
  }
}
