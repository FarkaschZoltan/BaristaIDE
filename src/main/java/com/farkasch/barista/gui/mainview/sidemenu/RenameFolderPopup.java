package com.farkasch.barista.gui.mainview.sidemenu;

import com.farkasch.barista.gui.component.FolderDropdown.FolderDropdownItem;
import com.farkasch.barista.gui.component.WarningPopup;
import com.farkasch.barista.services.FileService;
import com.farkasch.barista.services.PersistenceService;
import java.io.File;
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
public class RenameFolderPopup extends Stage {

  @Autowired
  private FileService fileService;
  @Autowired
  private PersistenceService persistenceService;
  @Autowired
  private WarningPopup warningPopup;

  private TextField newFolderNameField;
  private Label newFolderNameLabel;
  private Button applyButton;
  private VBox windowLayout;
  private HBox fieldLayout;
  private HBox buttonLayout;
  private Scene scene;

  private FolderDropdownItem folderToRename;

  @PostConstruct
  private void init() {
    setTitle("Rename Folder");
    newFolderNameField = new TextField();
    newFolderNameLabel = new Label("Folder name: ");
    applyButton = new Button("Apply");
    buttonLayout = new HBox(applyButton);
    fieldLayout = new HBox(newFolderNameLabel, newFolderNameField);
    windowLayout = new VBox(fieldLayout, buttonLayout);

    scene = new Scene(windowLayout, 400, 200);
    scene.getStylesheets().add(
      Paths.get("src/main/java/com/farkasch/barista/style.css").toAbsolutePath().toUri().toString());

    newFolderNameLabel.setLabelFor(newFolderNameLabel);

    applyButton.setOnAction(click -> {
      String newFolderName = newFolderNameField.getText();
      if (!persistenceService.getOpenProject().getFolders().contains(folderToRename.getParentPath() + "\\" + newFolderName)){
        fileService.renameFolder(new File(folderToRename.getPath()), newFolderName, folderToRename);
        close();
      } else {
        warningPopup.showWindow("Error", "A folder with this name already exists!", null);
      }
    });
  }

  private void onLoad(FolderDropdownItem fileToRename){
    this.folderToRename = fileToRename;
    newFolderNameField.setText("");
  }

  public void showWindow(FolderDropdownItem fileToRename) {
    onLoad(fileToRename);
    setScene(scene);
    show();
  }
}
