package com.farkasch.barista.gui.mainview.topmenu;

import com.farkasch.barista.services.ProcessService;
import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;
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
import org.springframework.lang.Nullable;

public class OpenFileWindow extends Stage {

    private Label fileName;
    private Label fileNameLabel;
    private Label chooseFileLabel;
    private Button openFileButton;
    private GridPane fileNameLayout;
    private GridPane rootFolderSelector;
    private ScrollPane scrollPane;
    private VBox windowLayout;
    private HBox openButtonContainer;
    private Scene scene;

    private String filePath;

    public OpenFileWindow(Consumer<File> openFile){
        fileName = new Label("");
        fileNameLabel = new Label("Chosen File: ");
        chooseFileLabel = new Label("Choose a file: ");

        openFileButton = new Button("Open");

        rootFolderSelector = new GridPane();
        fileNameLayout = new GridPane();

        scrollPane = new ScrollPane(rootFolderSelector);
        openButtonContainer = new HBox(openFileButton);
        windowLayout = new VBox(fileNameLayout, scrollPane, openButtonContainer);

        scene = new Scene(windowLayout, 300, 400);

        filePath = "";

        init(openFile);
    }

    private void init(Consumer<File> openFile){
        setTitle("Open File");

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

        folderExpand(null, null);
        setScene(scene);
    }

    private void folderExpand(@Nullable String parentName, @Nullable VBox parentContainer) {
        List<Pair<String, Boolean>> dirs = ProcessService.getDirsAndFiles(parentName);
        GridPane folderSelector = null;
        if (parentContainer == null) {
            folderSelector = rootFolderSelector;
        } else {
            folderSelector = new GridPane();
            folderSelector.setMaxWidth(Double.MAX_VALUE);
        }

        for (int i = 0; i < dirs.size(); i++) {
            VBox folderContainer = new VBox();
            folderContainer.setPrefWidth(
                parentContainer == null ? scene.getWidth() : parentContainer.getWidth());
            Label folderLabel = new Label(dirs.get(i).getKey());
            Boolean isFile = dirs.get(i).getValue();
            System.out.println(isFile);
            folderLabel.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
                Label target = ((Label) mouseEvent.getTarget());
                VBox parent = (VBox) (target.getParent());
                if (parent.getChildren().size() > 1) {
                    folderClose(parent);
                } else if(!isFile.booleanValue()){
                    folderExpand((parentName == null ? "" : parentName) + "\\" + target.getText(),
                        folderContainer);
                } else {
                    fileName.setText(target.getText());
                    filePath = "C:\\Users\\" + parentName + "\\" + target.getText();
                }
            });
            folderLabel.setId("folder");
            folderLabel.setMaxWidth(Double.MAX_VALUE);
            folderLabel.setMaxHeight(Double.MAX_VALUE);
            folderContainer.getChildren().add(folderLabel);
            folderSelector.addRow(i, folderContainer);
            if (parentContainer != null) {
                folderSelector.setPadding(new Insets(0, 0, 0, 20));
            }
            //folderSelector.setGridLinesVisible(true);
        }
        if (parentContainer != null) {
            parentContainer.getChildren().add(folderSelector);
        }
    }

    private void folderClose(VBox parent) {
        System.out.println(parent);
        parent.getChildren().remove(1, parent.getChildren().size());
    }

}
