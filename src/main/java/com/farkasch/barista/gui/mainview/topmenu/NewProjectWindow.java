package com.farkasch.barista.gui.mainview.topmenu;

import com.farkasch.barista.gui.component.FolderDropdown;
import com.farkasch.barista.services.FileService;
import com.farkasch.barista.services.PersistenceService;
import com.farkasch.barista.services.ProcessService;
import com.farkasch.barista.util.BaristaProject;
import com.farkasch.barista.util.enums.ProjectTypeEnum;
import java.nio.file.Paths;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NewProjectWindow extends Stage {

  @Autowired
  private ProcessService processService;
  @Autowired
  private FileService fileService;
  @Autowired
  private PersistenceService persistenceService;

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
  private void init(){
    setTitle("New Project");

    projectNameField = new TextField("New Project");
    projectNameLabel = new Label("Project name: ");

    folderPathField = new TextField("C:\\Users");
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
    projectType.setValue(ProjectTypeEnum.BASIC);

    createButton.setOnAction(actionEvent -> {
      boolean maven;
      boolean gradle;
      switch(projectType.getValue()){
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
      BaristaProject baristaProject = new BaristaProject(projectNameField.getText(), folderPathField.getText() + "\\" + projectNameField.getText(), maven, gradle);
      fileService.createNewProject(baristaProject);
      persistenceService.setOpenProject(baristaProject);
      close();
    });

    createButtonContainer.setAlignment(Pos.BOTTOM_RIGHT);
    VBox.setMargin(createButtonContainer, new Insets(10));

    rootFolderSelector = new FolderDropdown(scene.getWidth(), processService, false, false);
    rootFolderSelector.setFolderClickAction((parentName, parentContainer, target) -> folderPathField.setText("C:\\Users" + parentName + "\\" + target.getText()));

    scrollPane.setContent(rootFolderSelector);
  }

  public void showWindow(){
    rootFolderSelector.prepare(null, null);
    setScene(scene);

    show();
  }
}
