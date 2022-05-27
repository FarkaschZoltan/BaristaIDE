package com.farkasch.barista.gui.mainview.topmenu;

import com.farkasch.barista.gui.component.FolderDropdown;
import com.farkasch.barista.gui.component.WarningPopup;
import com.farkasch.barista.gui.mainview.sidemenu.SideMenu;
import com.farkasch.barista.services.FileService;
import com.farkasch.barista.services.PersistenceService;
import java.io.File;
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
public class OpenFileWindow extends Stage {

  @Autowired
  private FileService fileService;
  @Autowired
  private PersistenceService persistenceService;
  @Autowired
  private SideMenu sideMenu;
  @Autowired
  private WarningPopup warningPopup;

  private TextField fileName;
  private Label fileNameLabel;
  private Label chooseFileLabel;
  private Button openFileButton;
  private GridPane fileNameLayout;
  private FolderDropdown rootFolderSelector;
  private ScrollPane scrollPane;
  private VBox windowLayout;
  private HBox openButtonContainer;
  private Scene scene;

  private String filePath;
  private Consumer<File> openFile;

  @PostConstruct
  private void init() {
    setTitle("Open File");

    fileName = new TextField("");
    fileNameLabel = new Label("Chosen File: ");
    chooseFileLabel = new Label("Choose a file: ");

    openFileButton = new Button("Open");

    fileNameLayout = new GridPane();

    scrollPane = new ScrollPane();
    openButtonContainer = new HBox(openFileButton);
    windowLayout = new VBox(fileNameLayout, scrollPane, openButtonContainer);

    scene = new Scene(windowLayout, 300, 400);
    scene.getStylesheets().add(
      Paths.get("src/main/java/com/farkasch/barista/style.css").toAbsolutePath().toUri()
        .toString());

    filePath = "";

    scrollPane.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
    scrollPane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
    scrollPane.setPrefHeight(scene.getHeight());
    VBox.setMargin(scrollPane, new Insets(10));

    fileNameLabel.setLabelFor(fileName);

    fileName.setEditable(false);
    GridPane.setFillWidth(fileName, true);
    GridPane.setHgrow(fileName, Priority.ALWAYS);

    fileNameLayout.add(fileNameLabel, 0, 0);
    GridPane.setMargin(fileNameLabel, new Insets(10, 10, 10, 0));
    GridPane.setValignment(fileNameLabel, VPos.CENTER);
    fileNameLayout.add(fileName, 1, 0);
    fileNameLayout.add(chooseFileLabel, 0, 1);
    GridPane.setMargin(chooseFileLabel, new Insets(10, 0, 0, 0));
    VBox.setMargin(fileNameLayout, new Insets(10, 10, 0, 10));

    openFileButton.setOnAction(actionEvent -> {
      if(!filePath.equals("")){
        File file = new File(filePath);
        if (persistenceService.getOpenProject() != null) {
          sideMenu.closeProject(false);
        }
        openFile.accept(file);
        close();
      } else {
        warningPopup.showWindow("Error", "File name field must not be empty!", null);
      }
    });

    openButtonContainer.setAlignment(Pos.BOTTOM_RIGHT);
    VBox.setMargin(openButtonContainer, new Insets(10));
    initModality(Modality.APPLICATION_MODAL);
    setResizable(false);
  }

  private void onLoad(Consumer<File> openFile) {
    this.openFile = openFile;
    fileName.setText("");
    HashMap<String, String> styleIds = new HashMap<>();
    styleIds.put("item", "folder-dropdown__item");
    styleIds.put("dragEntered", "folder-dropdown__item--on-drag-entered");
    styleIds.put("graphic", "folder-dropdown__graphic");

    rootFolderSelector = new FolderDropdown(scene.getWidth(), fileService, warningPopup, styleIds, true, false);
    rootFolderSelector.setFileLeftClickAction(target -> {
      fileName.setText(target.getText());
      filePath = target.getParentPath() == null ? System.getProperty("user.home") + "\\" + target.getText() : target.getPath();

      if (rootFolderSelector.getLastClicked() != null) {
        rootFolderSelector.getLastClicked().setId(styleIds.get("item"));
      }
      target.setId("folder-dropdown__item--selected");
    });
    rootFolderSelector.prepare(null, null);
    scrollPane.setContent(rootFolderSelector);
  }

  public void showWindow(Consumer<File> openFile) {
    onLoad(openFile);
    setScene(scene);
    show();
  }
}
