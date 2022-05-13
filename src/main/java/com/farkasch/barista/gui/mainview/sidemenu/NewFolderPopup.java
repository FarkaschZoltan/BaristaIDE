package com.farkasch.barista.gui.mainview.sidemenu;

import com.farkasch.barista.gui.component.FolderDropdown.FolderDropdownItem;
import com.farkasch.barista.gui.component.WarningPopup;
import com.farkasch.barista.services.FileService;
import com.farkasch.barista.services.PersistenceService;
import java.nio.file.Paths;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NewFolderPopup extends Stage {

  @Autowired
  private FileService fileService;
  @Autowired
  private PersistenceService persistenceService;
  @Autowired
  private WarningPopup warningPopup;

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
    setTitle("Create New Folder");
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
      if (!persistenceService.getOpenProject().getFolders().contains(creationFolder.getPath() + "\\" + folderNameField.getText())) {
        fileService.createFolder(creationFolder.getPath() + "\\" + folderNameField.getText(), creationFolder);
        close();
      } else {
        warningPopup.showWindow("Error", "A folder with this name already exists!", null);
      }
    });
    initModality(Modality.APPLICATION_MODAL);
    setResizable(false);
  }

  private void onLoad(FolderDropdownItem creationFolder){
    this.creationFolder = creationFolder;
    folderNameField.setText("");
  }

  public void showWindow(FolderDropdownItem creationFolder) {
    onLoad(creationFolder);
    setScene(scene);
    show();
  }
}
