package com.farkasch.barista.gui.mainview.topmenu;

import com.farkasch.barista.gui.component.FolderDropdown;
import com.farkasch.barista.gui.component.FolderDropdown.FolderConsumer;
import com.farkasch.barista.services.ProcessService;
import com.sun.javafx.scene.control.LabeledText;
import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Pair;
import javax.annotation.PostConstruct;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component
public class OpenFileWindow extends Stage {

  @Autowired
  ProcessService processService;

  private Label fileName;
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

    fileName = new Label("");
    fileNameLabel = new Label("Chosen File: ");
    chooseFileLabel = new Label("Choose a file: ");

    openFileButton = new Button("Open");

    fileNameLayout = new GridPane();

    scrollPane = new ScrollPane();
    openButtonContainer = new HBox(openFileButton);
    windowLayout = new VBox(fileNameLayout, scrollPane, openButtonContainer);

    scene = new Scene(windowLayout, 300, 400);

    filePath = "";

    scene.getStylesheets().add(
      Paths.get("src/main/java/com/farkasch/barista/style.css").toAbsolutePath().toUri()
        .toString());

    scrollPane.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
    scrollPane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
    scrollPane.setPrefHeight(scene.getHeight());
    VBox.setMargin(scrollPane, new Insets(10));

    fileNameLabel.setLabelFor(fileName);

    fileNameLayout.add(fileNameLabel, 0, 0);
    GridPane.setMargin(fileNameLabel, new Insets(10, 20, 10, 10));
    GridPane.setValignment(fileNameLabel, VPos.CENTER);
    fileNameLayout.add(fileName, 1, 0);
    fileNameLayout.add(chooseFileLabel, 0, 1);
    GridPane.setMargin(chooseFileLabel, new Insets(10, 0, 0, 10));

    openFileButton.setOnAction(actionEvent -> {
      File file = new File(filePath);
      openFile.accept(file);
      close();
    });

    openButtonContainer.setAlignment(Pos.BOTTOM_RIGHT);
    VBox.setMargin(openButtonContainer, new Insets(10));

    rootFolderSelector = new FolderDropdown(scene, processService, true);
    rootFolderSelector.setFileClickAction((parentName, parentContainer, target) -> {
      fileName.setText(target.getText());
      filePath = "C:\\Users\\" + parentName + "\\" + target.getText();
    });

    scrollPane.setContent(rootFolderSelector);
  }

  public void showWindow(Consumer<File> openFile) {
    this.openFile = openFile;

    rootFolderSelector.folderExpand(null, null);
    setScene(scene);

    show();
  }
}
