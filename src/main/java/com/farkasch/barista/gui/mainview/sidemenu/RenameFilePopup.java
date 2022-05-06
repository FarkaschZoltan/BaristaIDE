package com.farkasch.barista.gui.mainview.sidemenu;

import com.farkasch.barista.gui.component.FolderDropdown.FolderDropdownItem;
import com.farkasch.barista.gui.component.WarningPopup;
import com.farkasch.barista.services.FileService;
import com.farkasch.barista.services.PersistenceService;
import com.google.common.io.Files;
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
public class RenameFilePopup extends Stage {

  @Autowired
  private FileService fileService;
  @Autowired
  private PersistenceService persistenceService;
  @Autowired
  private WarningPopup warningPopup;

  private TextField newFileNameField;
  private Label newFileNameLabel;
  private Button applyButton;
  private VBox windowLayout;
  private HBox fieldLayout;
  private HBox buttonLayout;
  private Scene scene;

  private FolderDropdownItem fileToRename;

  @PostConstruct
  private void init() {
    setTitle("Rename File");
    newFileNameField = new TextField();
    newFileNameLabel = new Label("File name: ");
    applyButton = new Button("Apply");
    buttonLayout = new HBox(applyButton);
    fieldLayout = new HBox(newFileNameLabel, newFileNameField);
    windowLayout = new VBox(fieldLayout, buttonLayout);

    scene = new Scene(windowLayout, 400, 200);
    scene.getStylesheets().add(
      Paths.get("src/main/java/com/farkasch/barista/style.css").toAbsolutePath().toUri().toString());

    newFileNameLabel.setLabelFor(newFileNameLabel);

    applyButton.setOnAction(click -> {
      String newFileName = newFileNameField.getText();
      if (newFileName.split("\\.").length < 2) {
        newFileName = newFileName.concat("." + Files.getFileExtension(fileToRename.getPath()));
      }
      if (!persistenceService.getOpenProject().getSourceFiles().contains(fileToRename.getParentPath() + "\\" + newFileName)){
        fileService.renameFile(new File(fileToRename.getPath()), newFileName, fileToRename);
        close();
      } else {
        warningPopup.showWindow("Error", "A file with this name already exists!", null);
      }
    });
  }

  public void showWindow(FolderDropdownItem fileToRename) {
    this.fileToRename = fileToRename;
    setScene(scene);
    show();
  }
}
