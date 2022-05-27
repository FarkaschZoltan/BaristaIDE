package com.farkasch.barista.gui.mainview.topmenu;

import com.farkasch.barista.gui.component.WarningPopup;
import com.farkasch.barista.services.FileService;
import com.farkasch.barista.util.BaristaProject;
import java.nio.file.Paths;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javax.annotation.PostConstruct;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LoadProjectWindow extends Stage {

  @Autowired
  private FileService fileService;
  @Autowired
  private WarningPopup warningPopup;

  private Label projectsLabel;
  private Label chosenProjectLabel;
  private Button openButton;
  private GridPane fieldLayout;
  private ScrollPane scrollPane;
  private VBox openButtonContainer;
  private VBox projectsContainer;
  private VBox windowLayout;
  private TextField chosenProject;
  private Scene scene;
  private BaristaProject selectedProject;

  @PostConstruct
  private void init(){
    setTitle("Load Project");

    chosenProject = new TextField();
    fieldLayout = new GridPane();
    scrollPane = new ScrollPane();
    projectsContainer = new VBox();
    openButtonContainer = new VBox();
    windowLayout = new VBox();
    projectsLabel = new Label("Choose project: ");
    chosenProjectLabel = new Label("Chosen project: ");
    openButton = new Button("Open");
    selectedProject = new BaristaProject();

    windowLayout.getChildren().addAll(fieldLayout, scrollPane, openButtonContainer);

    scene = new Scene(windowLayout, 300, 400);

    chosenProject.setEditable(false);
    chosenProjectLabel.setLabelFor(chosenProject);

    scrollPane.setContent(projectsContainer);
    scrollPane.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
    scrollPane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
    scrollPane.setPrefHeight(scene.getHeight());
    VBox.setMargin(scrollPane, new Insets(0, 10, 10, 10));
    scrollPane.setContent(projectsContainer);

    projectsContainer.setMinWidth(scene.getWidth() - 20.0);
    projectsLabel.setLabelFor(projectsContainer);

    fieldLayout.add(chosenProjectLabel, 0, 0);
    GridPane.setMargin(chosenProjectLabel, new Insets(10, 10, 10, 0));
    GridPane.setValignment(chosenProjectLabel, VPos.CENTER);
    fieldLayout.add(chosenProject, 1, 0);
    GridPane.setHgrow(chosenProject, Priority.ALWAYS);
    GridPane.setFillWidth(chosenProject, true);
    fieldLayout.add(projectsLabel, 0, 1);
    GridPane.setMargin(projectsLabel, new Insets(10, 0, 0, 0));
    GridPane.setValignment(projectsLabel, VPos.CENTER);
    VBox.setMargin(fieldLayout, new Insets(10));

    openButton.setOnAction(click -> {
      if(selectedProject.getProjectName() != null){
        fileService.loadProject(selectedProject);
        close();
      } else {
        warningPopup.showWindow("Error", "Please Select a Project!", null);
      }
    });

    openButtonContainer.getChildren().add(openButton);
    openButtonContainer.setAlignment(Pos.BOTTOM_RIGHT);
    VBox.setMargin(openButtonContainer, new Insets(10));

    scene.getStylesheets().add(Paths.get("src/main/java/com/farkasch/barista/style.css").toAbsolutePath().toUri().toString());

    initModality(Modality.APPLICATION_MODAL);
    setResizable(false);
  }

  private void onLoad(){
    chosenProject.setText("");
    List<BaristaProject> projects = fileService.getProjects();
    projectsContainer.getChildren().clear();
    for(BaristaProject project : projects){
      Button openProject = new Button(project.getProjectName());
      openProject.setId("folder-dropdown__item");
      openProject.setGraphic(new FontIcon("mdi-folder"));
      openProject.setOnAction(click -> {
        chosenProject.setText(project.getProjectName());
        selectedProject = project;
      });
      openProject.setMinWidth(projectsContainer.getMinWidth());
      openProject.setMaxWidth(Double.MAX_VALUE);
      openProject.setMaxHeight(Double.MAX_VALUE);
      projectsContainer.getChildren().add(openProject);
    }
  }

  public void showWindow(){
    onLoad();
    setScene(scene);
    show();
  }
}
