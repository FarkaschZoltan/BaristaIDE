package com.farkasch.barista.gui.mainview.sidemenu;

import com.farkasch.barista.services.FileService;
import com.farkasch.barista.services.PersistenceService;
import com.farkasch.barista.util.FileTemplates;
import com.farkasch.barista.util.enums.FileExtensionEnum;
import com.farkasch.barista.util.enums.JavaClassTypesEnum;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NewFilePopup extends Stage {

  @Autowired
  private FileService fileService;
  @Autowired
  private PersistenceService persistenceService;
  @Autowired
  private FileTemplates fileTemplates;
  private TextField fileNameField;
  private ComboBox<FileExtensionEnum> fileExtensionComboBox;
  private ComboBox<JavaClassTypesEnum> classTypeComboBox;
  private Label fileNameLabel;
  private Label classTypeLabel;
  private Button createButton;
  private VBox windowLayout;
  private HBox fieldLayout;
  private HBox classTypeLayout;
  private HBox buttonLayout;
  private Scene scene;

  private String fileParentPath;
  private ObservableList<FileExtensionEnum> fileExtensions = FXCollections.observableArrayList(FileExtensionEnum.JAVA, FileExtensionEnum.TXT, FileExtensionEnum.XML,
    FileExtensionEnum.OTHER);
  private ObservableList<JavaClassTypesEnum> classTypes = FXCollections.observableArrayList(JavaClassTypesEnum.CLASS, JavaClassTypesEnum.ENUM, JavaClassTypesEnum.INTERFACE,
    JavaClassTypesEnum.ANNOTATION, JavaClassTypesEnum.RECORD);

  @PostConstruct
  private void init() {
    fileNameField = new TextField();
    fileExtensionComboBox = new ComboBox<>();
    classTypeComboBox = new ComboBox<>();
    fileNameLabel = new Label("File Name: ");
    classTypeLabel = new Label("Class preset: ");
    createButton = new Button("Create");
    fieldLayout = new HBox(fileNameLabel, fileNameField, fileExtensionComboBox);
    classTypeLayout = new HBox(classTypeLabel, classTypeComboBox);
    buttonLayout = new HBox(createButton);
    windowLayout = new VBox(fieldLayout, classTypeLayout, buttonLayout);

    fileParentPath = "";

    scene = new Scene(windowLayout, 200, 400);
    scene.getStylesheets().add(
      Paths.get("src/main/java/com/farkasch/barista/style.css").toAbsolutePath().toUri()
        .toString());

    fileExtensionComboBox.setItems(fileExtensions);
    fileExtensionComboBox.setOnAction(actionEvent -> {
      if(fileExtensionComboBox.getValue() == FileExtensionEnum.JAVA){
        classTypeLayout.setVisible(true);
      } else {
        classTypeLayout.setVisible(false);
      }
    });

    fileNameLabel.setLabelFor(fileNameField);

    classTypeLabel.setLabelFor(classTypeComboBox);
    classTypeComboBox.setItems(classTypes);
    classTypeLayout.setVisible(false);

    createButton.setOnAction(click -> {
      try {
        String extension;
        if (fileExtensionComboBox.getValue().equals(FileExtensionEnum.OTHER)) {
          if (fileNameField.getText().split("\\.").length > 1) {
            extension = "";
          } else {
            extension = ".txt";
          }
        } else {
          extension = fileExtensionComboBox.getValue().getName();
        }

        System.out.println("extension: " + extension);
        File newFile = fileService.createFile(fileParentPath + "\\" + fileNameField.getText() + extension);

        if (fileExtensionComboBox.getValue().equals(FileExtensionEnum.JAVA)) {
          FileWriter writer = new FileWriter(newFile);
          switch (classTypeComboBox.getValue()) {
            case ENUM:
              writer.write(fileTemplates.enumTemplate(fileNameField.getText(), fileParentPath));
              break;
            case CLASS:
              writer.write(fileTemplates.classTemplate(fileNameField.getText(), fileParentPath));
              break;
            case RECORD:
              writer.write(fileTemplates.recordTemplate(fileNameField.getText(), fileParentPath));
              break;
            case INTERFACE:
              writer.write(fileTemplates.interfaceTemplate(fileNameField.getText(), fileParentPath));
              break;
            case ANNOTATION:
              writer.write(fileTemplates.annotationTemplate(fileNameField.getText(), fileParentPath));
              break;
            default:
              break;
          }
          writer.close();
        }

        persistenceService.openNewFile(newFile);
        persistenceService.setActiveFile(newFile);

        close();

      } catch (FileAlreadyExistsException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
  }

  public void showWindow(String fileParentPath) {
    this.fileParentPath = fileParentPath;

    setScene(scene);
    show();
  }
}
