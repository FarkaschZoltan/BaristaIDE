package com.farkasch.barista.gui.mainview.sidemenu;

import com.farkasch.barista.gui.codinginterface.CodingInterfaceContainer;
import com.farkasch.barista.gui.component.ErrorPopup;
import com.farkasch.barista.gui.component.FolderDropdown.FolderDropdownItem;
import com.farkasch.barista.gui.component.WarningPopup;
import com.farkasch.barista.services.FileService;
import com.farkasch.barista.services.PersistenceService;
import com.farkasch.barista.util.FileTemplates;
import com.farkasch.barista.util.Result;
import com.farkasch.barista.util.enums.FileExtensionEnum;
import com.farkasch.barista.util.enums.JavaClassTypesEnum;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Paths;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class NewFilePopup extends Stage {

  @Autowired
  private FileService fileService;
  @Autowired
  private PersistenceService persistenceService;
  @Autowired
  private FileTemplates fileTemplates;
  @Autowired
  private ErrorPopup errorPopup;
  @Autowired
  private WarningPopup warningPopup;
  @Lazy
  @Autowired
  private CodingInterfaceContainer codingInterfaceContainer;

  //Design
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

  private FolderDropdownItem creationFolder;
  private ObservableList<FileExtensionEnum> fileExtensions = FXCollections.observableArrayList(FileExtensionEnum.JAVA, FileExtensionEnum.TXT,
    FileExtensionEnum.XML,
    FileExtensionEnum.OTHER);
  private ObservableList<JavaClassTypesEnum> classTypes = FXCollections.observableArrayList(JavaClassTypesEnum.CLASS, JavaClassTypesEnum.ENUM,
    JavaClassTypesEnum.INTERFACE,
    JavaClassTypesEnum.ANNOTATION, JavaClassTypesEnum.RECORD);

  @PostConstruct
  private void init() {
    setTitle("Create New File");
    fileNameField = new TextField();
    fileExtensionComboBox = new ComboBox<>();
    classTypeComboBox = new ComboBox<>();
    fileNameLabel = new Label("File Name: ");
    classTypeLabel = new Label("Class Preset: ");
    createButton = new Button("Create");
    fieldLayout = new HBox(fileNameLabel, fileNameField, fileExtensionComboBox);
    classTypeLayout = new HBox(classTypeLabel, classTypeComboBox);
    buttonLayout = new HBox(createButton);
    windowLayout = new VBox(fieldLayout, classTypeLayout, buttonLayout);

    scene = new Scene(windowLayout, 400, 200);
    scene.getStylesheets().add(Paths.get("src/main/java/com/farkasch/barista/style.css").toAbsolutePath().toUri().toString());

    fileExtensionComboBox.setItems(fileExtensions);
    fileExtensionComboBox.setOnAction(actionEvent -> {
      if (fileExtensionComboBox.getValue() == FileExtensionEnum.JAVA) {
        classTypeLayout.setVisible(true);
      } else {
        classTypeLayout.setVisible(false);
      }
    });

    fileNameLabel.setLabelFor(fileNameField);
    fileExtensionComboBox.setValue(FileExtensionEnum.JAVA);

    classTypeLabel.setLabelFor(classTypeComboBox);
    classTypeComboBox.setItems(classTypes);
    classTypeComboBox.setValue(JavaClassTypesEnum.CLASS);
    classTypeLayout.setVisible(true);

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

        if(!persistenceService.getOpenProject().getSourceFiles().contains(creationFolder.getPath() + "\\" + fileNameField.getText() + extension)) {
          File newFile = fileService.createFile(creationFolder.getPath() + "\\" + fileNameField.getText() + extension, creationFolder);

          if (fileExtensionComboBox.getValue().equals(FileExtensionEnum.JAVA)) {
            FileWriter writer = new FileWriter(newFile);
            switch (classTypeComboBox.getValue()) {
              case ENUM:
                writer.write(fileTemplates.enumTemplate(fileNameField.getText(), creationFolder.getPath()));
                break;
              case CLASS:
                writer.write(fileTemplates.classTemplate(fileNameField.getText(), creationFolder.getPath()));
                break;
              case RECORD:
                writer.write(fileTemplates.recordTemplate(fileNameField.getText(), creationFolder.getPath()));
                break;
              case INTERFACE:
                writer.write(fileTemplates.interfaceTemplate(fileNameField.getText(), creationFolder.getPath()));
                break;
              case ANNOTATION:
                writer.write(fileTemplates.annotationTemplate(fileNameField.getText(), creationFolder.getPath()));
                break;
              default:
                break;
            }
            writer.close();
          }

          codingInterfaceContainer.openFile(newFile);
          persistenceService.setActiveFile(newFile);
          close();
        } else {
          warningPopup.showWindow("Error", "A file with this name already exists!", null);
        }
      } catch (IOException e) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        File errorFile = fileService.createErrorLog(stringWriter.toString());
        errorPopup.showWindow(Result.ERROR("Error while creating new file!", errorFile));

        printWriter.close();
        e.printStackTrace();
      }
    });
    initModality(Modality.APPLICATION_MODAL);
    setResizable(false);
  }

  private void onLoad(FolderDropdownItem creationFolder){
    this.creationFolder = creationFolder;
    fileNameField.setText("");
    fileExtensionComboBox.setValue(FileExtensionEnum.JAVA);
    classTypeComboBox.setValue(JavaClassTypesEnum.CLASS);
  }

  public void showWindow(FolderDropdownItem creationFolder) {
    onLoad(creationFolder);
    setScene(scene);
    show();
  }
}
