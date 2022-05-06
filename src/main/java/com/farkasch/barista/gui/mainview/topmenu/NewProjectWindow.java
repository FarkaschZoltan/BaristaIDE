package com.farkasch.barista.gui.mainview.topmenu;

import com.farkasch.barista.gui.component.FolderDropdown;
import com.farkasch.barista.services.FileService;
import com.farkasch.barista.util.BaristaProject;
import com.farkasch.barista.util.enums.ProjectTypeEnum;
import java.nio.file.Paths;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NewProjectWindow extends Stage {

  @Autowired
  private FileService fileService;

  //Design
  private TextField projectNameField;
  private TextField folderPathField;
  private ComboBox<ProjectTypeEnum> projectType;
  private FolderDropdown rootFolderSelector;
  private Label projectNameLabel;
  private Label folderPathLabel;
  private Label projectTypeLabel;
  private Label rootFolderLabel;
  private Button createButton;
  private GridPane fieldLayout;
  private ScrollPane scrollPane;
  private VBox windowLayout;
  private HBox createButtonContainer;
  private Scene scene;

  @PostConstruct
  private void init() {
    setTitle("New Project");

    projectNameField = new TextField("New Project");
    projectNameLabel = new Label("Project name: ");

    folderPathField = new TextField(System.getProperty("user.home"));
    folderPathLabel = new Label("Project creation folder: ");

    projectType = new ComboBox<>();
    projectTypeLabel = new Label("Project Type: ");

    createButton = new Button("Create");
    createButtonContainer = new HBox(createButton);

    fieldLayout = new GridPane();

    rootFolderLabel = new Label("Choose destination folder: ");
    scrollPane = new ScrollPane();

    windowLayout = new VBox(fieldLayout, scrollPane, createButtonContainer);

    scene = new Scene(windowLayout, 300, 400);
    scene.getStylesheets().add(
      Paths.get("src/main/java/com/farkasch/barista/style.css").toAbsolutePath().toUri()
        .toString());

    scrollPane.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
    scrollPane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
    scrollPane.setPrefHeight(scene.getHeight());
    VBox.setMargin(scrollPane, new Insets(10));

    projectNameLabel.setLabelFor(projectNameField);
    folderPathLabel.setLabelFor(folderPathField);
    projectNameLabel.setLabelFor(projectType);

    fieldLayout.add(projectNameLabel, 0, 0);
    GridPane.setMargin(projectNameLabel, new Insets(10, 20, 10, 10));
    GridPane.setValignment(projectNameLabel, VPos.CENTER);
    fieldLayout.add(projectNameField, 1, 0);
    fieldLayout.add(folderPathLabel, 0, 1);
    GridPane.setMargin(folderPathLabel, new Insets(10, 20, 10, 10));
    GridPane.setValignment(folderPathLabel, VPos.CENTER);
    fieldLayout.add(folderPathField, 1, 1);
    fieldLayout.add(projectTypeLabel, 0, 2);
    GridPane.setMargin(projectTypeLabel, new Insets(10, 20, 10, 10));
    GridPane.setValignment(projectTypeLabel, VPos.CENTER);
    fieldLayout.add(projectType, 1, 2);
    fieldLayout.add(rootFolderLabel, 0, 3);
    GridPane.setMargin(rootFolderLabel, new Insets(10, 0, 0, 10));

    projectType.setItems(FXCollections.observableArrayList(ProjectTypeEnum.BASIC, ProjectTypeEnum.MAVEN, ProjectTypeEnum.GRADLE));
    projectType.setConverter(new StringConverter<>() {
      @Override
      public String toString(ProjectTypeEnum projectTypeEnum) {
        return projectTypeEnum.getName();
      }

      @Override
      public ProjectTypeEnum fromString(String s) {
        return ProjectTypeEnum.valueOf(s.toUpperCase());
      }
    });

    createButton.setOnAction(actionEvent -> {
      boolean maven;
      boolean gradle;
      switch (projectType.getValue()) {
        case MAVEN:
          maven = true;
          gradle = false;
          break;
        case GRADLE:
          maven = false;
          gradle = true;
          break;
        default:
          maven = false;
          gradle = false;
      }
      BaristaProject baristaProject = new BaristaProject(projectNameField.getText(), folderPathField.getText() + "\\" + projectNameField.getText(),
        maven, gradle);
      fileService.createNewProject(baristaProject);
      close();
    });

    createButtonContainer.setAlignment(Pos.BOTTOM_RIGHT);
    VBox.setMargin(createButtonContainer, new Insets(10));
  }

  private void onLoad(){
    projectNameField.setText("");
    folderPathField.setText(System.getProperty("user.home"));
    projectType.setValue(ProjectTypeEnum.BASIC);

    rootFolderSelector = new FolderDropdown(scene.getWidth(), fileService, false, false);
    rootFolderSelector.setFolderLeftClickAction(target -> folderPathField.setText(
      target.getParentPath() == null ? System.getProperty("user.home") + "\\" + target.getText() : target.getPath()));

    rootFolderSelector.prepare(null, null);
    scrollPane.setContent(rootFolderSelector);
  }

  public void showWindow() {
    onLoad();
    setScene(scene);
    show();
  }
}
