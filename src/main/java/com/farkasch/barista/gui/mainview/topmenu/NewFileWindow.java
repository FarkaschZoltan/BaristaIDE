package com.farkasch.barista.gui.mainview.topmenu;

import com.farkasch.barista.services.FileService;
import com.farkasch.barista.services.ProcessService;
import java.io.File;
import java.nio.file.FileAlreadyExistsException;
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
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.lang.Nullable;

public class NewFileWindow extends Stage {

    //Design
    private TextField fileNameField;
    private TextField folderPathField;
    private Label folderPathLabel;
    private Label fileNameLabel;
    private Label folderSelectorLabel;
    private Button createButton;
    private GridPane fieldLayout;
    private GridPane rootFolderSelector;
    private ScrollPane scrollPane;
    private VBox windowLayout;
    private HBox createButtonContainer;
    private Scene scene;


    public NewFileWindow(Consumer<File> openFile) {
        fileNameField = new TextField("NewFile.txt");
        fileNameLabel = new Label("File name: ");

        folderPathField = new TextField("C:\\Users");
        folderPathLabel = new Label("Folder path: ");

        createButton = new Button("Create");
        createButtonContainer = new HBox(createButton);

        fieldLayout = new GridPane();

        rootFolderSelector = new GridPane();
        folderSelectorLabel = new Label("Folders: ");
        scrollPane = new ScrollPane(rootFolderSelector);

        windowLayout = new VBox(fieldLayout, scrollPane, createButtonContainer);

        scene = new Scene(windowLayout, 300, 400);

        init(openFile);
    }

    private void init(Consumer<File> openFile) {
        setTitle("New File");

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
                File newFile = FileService.createFile(
                    folderPathField.getText() + "\\" + fileNameField.getText());
                openFile.accept(newFile);
                close();
            } catch (FileAlreadyExistsException e) {
                e.printStackTrace();
            }
        });

        createButtonContainer.setAlignment(Pos.BOTTOM_RIGHT);
        VBox.setMargin(createButtonContainer, new Insets(10));

        folderExpand(null, null);
        setScene(scene);
    }

    private void folderExpand(@Nullable String parentName, @Nullable VBox parentContainer) {
        List<String> dirs = ProcessService.getDirs(parentName);
        folderPathField.setText("C:\\Users" + (parentName == null ? "" : parentName));
        GridPane folderSelector = null;
        if (parentContainer == null) {
            folderSelector = rootFolderSelector;
        } else {
            folderSelector = new GridPane();
            folderSelector.setMaxWidth(Double.MAX_VALUE);
        }

        for (int i = 0; i < dirs.size(); i++) {
            VBox folderContainer = new VBox();
            folderContainer.setMinWidth(
                parentContainer == null ? scene.getWidth() : parentContainer.getWidth());
            Label folderLabel = new Label(dirs.get(i));
            folderLabel.setGraphic(new FontIcon("mdi-folder"));
            folderLabel.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
                Label target = ((Label) mouseEvent.getTarget());
                VBox parent = (VBox) (target.getParent());
                if (parent.getChildren().size() > 1) {
                    folderClose(parent);
                } else {
                    folderExpand((parentName == null ? "" : parentName) + "\\" + target.getText(),
                        folderContainer);
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
