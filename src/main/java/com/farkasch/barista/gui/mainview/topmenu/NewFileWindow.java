package com.farkasch.barista.gui.mainview.topmenu;

import com.farkasch.barista.gui.component.ErrorPopup;
import com.farkasch.barista.gui.component.FolderDropdown;
import com.farkasch.barista.gui.component.WarningPopup;
import com.farkasch.barista.gui.mainview.sidemenu.SideMenu;
import com.farkasch.barista.services.FileService;
import com.farkasch.barista.services.PersistenceService;
import com.farkasch.barista.util.Result;
import com.farkasch.barista.util.enums.ResultTypeEnum;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Paths;
import java.util.HashMap;
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
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NewFileWindow extends Stage {

  @Autowired
  private FileService fileService;
  @Autowired
  private PersistenceService persistenceService;
  @Autowired
  private SideMenu sideMenu;
  @Autowired
  private WarningPopup warningPopup;

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

    scene = new Scene(windowLayout, 350, 400);

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
    GridPane.setMargin(fileNameLabel, new Insets(10, 10, 10, 0));
    GridPane.setValignment(fileNameLabel, VPos.CENTER);
    fieldLayout.add(fileNameField, 1, 0);
    GridPane.setFillWidth(fileNameField, true);
    GridPane.setHgrow(fileNameField, Priority.ALWAYS);
    fieldLayout.add(folderPathLabel, 0, 1);
    GridPane.setMargin(folderPathLabel, new Insets(10, 10, 10, 0));
    GridPane.setValignment(folderPathLabel, VPos.CENTER);
    fieldLayout.add(folderPathField, 1, 1);
    GridPane.setFillWidth(folderPathField, true);
    GridPane.setHgrow(folderPathField, Priority.ALWAYS);
    fieldLayout.add(folderSelectorLabel, 0, 2);
    GridPane.setMargin(folderSelectorLabel, new Insets(10, 0, 0, 0));
    VBox.setMargin(fieldLayout, new Insets(10, 10, 0, 10));

    createButton.setOnAction(actionEvent -> {
      if(!folderPathField.getText().equals("") && !fileNameField.getText().equals("")){
        Result fileCreated = fileService.createFile(folderPathField.getText() + "\\" + fileNameField.getText());
        if (fileCreated.getResult().equals(ResultTypeEnum.OK)) {
          File newFile = (File) fileCreated.getReturnValue();
          if (persistenceService.getOpenProject() != null) {
            sideMenu.closeProject(false);
          }
          openFile.accept(newFile);
          close();
        } else {
          warningPopup.showWindow(fileCreated);
        }
      } else {
        warningPopup.showWindow("Error", "File name field and folder path field must not be empty!", null);
      }
    });

    createButtonContainer.setAlignment(Pos.BOTTOM_RIGHT);
    VBox.setMargin(createButtonContainer, new Insets(10));

    initModality(Modality.APPLICATION_MODAL);
    setResizable(false);
  }

  private void onLoad() {
    fileNameField.setText("");
    folderPathField.setText(System.getProperty("user.home"));
    HashMap<String, String> styleIds = new HashMap<>();
    styleIds.put("item", "folder-dropdown__item");
    styleIds.put("dragEntered", "folder-dropdown__item--on-drag-entered");
    styleIds.put("graphic", "folder-dropdown__graphic");

    rootFolderSelector = new FolderDropdown(scene.getWidth() - 60, fileService, warningPopup, styleIds, false, false);
    rootFolderSelector.setFolderLeftClickAction(target -> {
      folderPathField.setText(target.getParentPath() == null ? System.getProperty("user.home") + "\\" + target.getText() : target.getPath());
      if (rootFolderSelector.getLastClicked() != null) {
        rootFolderSelector.getLastClicked().setId(styleIds.get("item"));
      }
      target.setId("folder-dropdown__item--selected");
    });

    scrollPane.setContent(rootFolderSelector);
  }

  public void showWindow(Consumer<File> openFile) {
    onLoad();
    this.openFile = openFile;
    rootFolderSelector.prepare(null, null);
    setScene(scene);

    show();
  }
}
