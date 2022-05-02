package com.farkasch.barista.gui.mainview.sidemenu;

import com.farkasch.barista.gui.component.FolderDropdown.FolderDropdownItem;
import com.farkasch.barista.services.FileService;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Paths;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NewFolderPopup extends Stage {

  @Autowired
  private FileService fileService;

  private TextField folderNameField;
  private Label folderNameLabel;
  private Button createButton;
  private VBox windowLayout;
  private HBox buttonLayout;
  private HBox fieldLayout;
  private Scene scene;

  private FolderDropdownItem creationFolder;

  @PostConstruct
  private void init() {
    folderNameField = new TextField();
    folderNameLabel = new Label("Folder Name: ");
    createButton = new Button("Create");
    buttonLayout = new HBox(createButton);
    fieldLayout = new HBox(folderNameLabel, folderNameField);
    windowLayout = new VBox(fieldLayout, buttonLayout);

    scene = new Scene(windowLayout, 400, 200);
    scene.getStylesheets().add(Paths.get("src/main/java/com/farkasch/barista/style.css").toAbsolutePath().toUri().toString());

    folderNameLabel.setLabelFor(folderNameField);

    createButton.setOnAction(click -> {
      try {
        File newFolder = fileService.createFolder(creationFolder.getPath() + "\\" + folderNameField.getText(), creationFolder);
        close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
  }

  public void showWindow(FolderDropdownItem creationFolder){
    this.creationFolder = creationFolder;
    setScene(scene);
    show();
  }
}
