package com.farkasch.barista.gui.mainview.sidemenu;

import com.farkasch.barista.gui.component.FolderDropdown;
import com.farkasch.barista.gui.component.FolderDropdown.FolderDropdownItem;
import com.farkasch.barista.gui.component.WarningPopup;
import com.farkasch.barista.services.FileService;
import com.farkasch.barista.services.PersistenceService;
import java.awt.event.ActionEvent;
import java.beans.EventHandler;
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
public abstract class AbstractRenamePopup extends Stage {

  @Autowired
  protected FileService fileService;
  @Autowired
  protected PersistenceService persistenceService;
  @Autowired
  protected WarningPopup warningPopup;

  protected TextField newNameField;
  protected Label newNameLabel;
  protected Button applyButton;
  protected VBox windowLayout;
  protected HBox fieldLayout;
  protected HBox buttonLayout;
  protected Scene scene;

  protected FolderDropdownItem itemToRename;

  @PostConstruct
  private void init(){
    newNameField = new TextField();
    newNameLabel = new Label("New name: ");
    applyButton = new Button("Apply");
    buttonLayout = new HBox(applyButton);
    fieldLayout = new HBox(newNameLabel, newNameField);
    windowLayout = new VBox(fieldLayout, buttonLayout);

    scene = new Scene(windowLayout, 400, 200);
    scene.getStylesheets().add(
      Paths.get("src/main/java/com/farkasch/barista/style.css").toAbsolutePath().toUri().toString());

    newNameLabel.setLabelFor(newNameLabel);

    applyButton.setOnAction(click -> save());
  }

  protected abstract void save();

  protected abstract void onLoad(FolderDropdownItem folderDropdownItem);

  public void showWindow(FolderDropdownItem folderDropdownItem){
    onLoad(folderDropdownItem);
    setScene(scene);
    show();
  }
}
