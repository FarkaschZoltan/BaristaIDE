package com.farkasch.barista.gui.mainview.topmenu;

import com.farkasch.barista.gui.component.FolderDropdown;
import com.farkasch.barista.services.FileService;
import com.farkasch.barista.services.ProcessService;
import java.io.File;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Paths;
import java.util.function.Consumer;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NewFileWindow extends Stage {

  @Autowired
  private FileService fileService;
  @Autowired
  private ProcessService processService;

  //Design
  private TextField fileNameField;
  private TextField folderPathField;
  private Label folderPathLabel;
  private Label fileNameLabel;
  private Label folderSelectorLabel;
  private Button createButton;
  private GridPane fieldLayout;
  private FolderDropdown rootFolderSelector;
  private ScrollPane scrollPane;
  private VBox windowLayout;
  private HBox createButtonContainer;
  private Scene scene;

  Consumer<File> openFile;

  @PostConstruct
  private void init() {
    setTitle("New File");

    fileNameField = new TextField("NewFile.txt");
    fileNameLabel = new Label("File name: ");

    folderPathField = new TextField(System.getProperty("user.home"));
    folderPathLabel = new Label("Folder path: ");

    createButton = new Button("Create");
    createButtonContainer = new HBox(createButton);

    fieldLayout = new GridPane();

    folderSelectorLabel = new Label("Choose destination folder: ");
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

    fileNameLabel.setLabelFor(fileNameField);
    folderPathLabel.setLabelFor(folderPathField);

    fieldLayout.add(fileNameLabel, 0, 0);
    GridPane.setMargin(fileNameLabel, new Insets(10, 20, 10, 10));
    GridPane.setValignment(fileNameLabel, VPos.CENTER);
    fieldLayout.add(fileNameField, 1, 0);
    fieldLayout.add(folderPathLabel, 0, 1);
    GridPane.setMargin(folderPathLabel, new Insets(10, 20, 10, 10));
    GridPane.setValignment(folderPathLabel, VPos.CENTER);
    fieldLayout.add(folderPathField, 1, 1);
    fieldLayout.add(folderSelectorLabel, 0, 2);
    GridPane.setMargin(folderSelectorLabel, new Insets(10, 0, 0, 10));

    createButton.setOnAction(actionEvent -> {
      try {
        File newFile = fileService.createFile(
          folderPathField.getText() + "\\" + fileNameField.getText());
        openFile.accept(newFile);
        close();
      } catch (FileAlreadyExistsException e) {
        e.printStackTrace();
      }
    });

    createButtonContainer.setAlignment(Pos.BOTTOM_RIGHT);
    VBox.setMargin(createButtonContainer, new Insets(10));

    rootFolderSelector = new FolderDropdown(scene.getWidth(), processService, false, false);
    rootFolderSelector.setFolderLeftClickAction((parentName, parentContainer, target) -> {
      folderPathField.setText(parentName == null ? System.getProperty("user.home") + "\\" + target.getText() : target.getPath());
    });

    scrollPane.setContent(rootFolderSelector);
  }

  public void showWindow(Consumer<File> openFile) {
    this.openFile = openFile;

    rootFolderSelector.prepare(null, null);
    setScene(scene);

    show();
  }
}
