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
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.StringConverter;
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
  private Region buttonAlignmentRegion;
  private VBox windowLayout;
  private GridPane fieldLayout;
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
    buttonAlignmentRegion = new Region();
    fieldLayout = new GridPane();
    buttonLayout = new HBox(buttonAlignmentRegion, createButton);
    windowLayout = new VBox(fieldLayout, buttonLayout);

    scene = new Scene(windowLayout, 500, 110);
    scene.getStylesheets().add(Paths.get("src/main/java/com/farkasch/barista/style.css").toAbsolutePath().toUri().toString());

    fileExtensionComboBox.setValue(FileExtensionEnum.JAVA);
    fileExtensionComboBox.setItems(fileExtensions);
    fileExtensionComboBox.setOnAction(actionEvent -> {
      if (fileExtensionComboBox.getValue() == FileExtensionEnum.JAVA) {
        classTypeLabel.setVisible(true);
        classTypeComboBox.setVisible(true);
      } else {
        classTypeLabel.setVisible(false);
        classTypeComboBox.setVisible(false);
      }
    });
    fileExtensionComboBox.setConverter(new StringConverter<>() {
      @Override
      public String toString(FileExtensionEnum fileExtensionEnum) {
        return fileExtensionEnum.getName();
      }

      @Override
      public FileExtensionEnum fromString(String s) {
        for (FileExtensionEnum item : fileExtensionComboBox.getItems()) {
          if (item.getName().equals(s)) {
            return item;
          }
        }
        return null;
      }
    });
    fileExtensionComboBox.setMaxWidth(Double.MAX_VALUE);
    GridPane.setHgrow(fileExtensionComboBox, Priority.ALWAYS);
    GridPane.setFillWidth(fileExtensionComboBox, true);

    fileNameLabel.setLabelFor(fileNameField);
    classTypeLabel.setLabelFor(classTypeComboBox);

    GridPane.setHgrow(fileNameField, Priority.ALWAYS);
    GridPane.setFillWidth(fileNameField, true);

    classTypeComboBox.setItems(classTypes);
    classTypeComboBox.setValue(JavaClassTypesEnum.CLASS);
    classTypeComboBox.setConverter(new StringConverter<>() {
      @Override
      public String toString(JavaClassTypesEnum javaClassTypesEnum) {
        return javaClassTypesEnum.getName();
      }

      @Override
      public JavaClassTypesEnum fromString(String s) {
        for(JavaClassTypesEnum item : classTypeComboBox.getItems()){
          if(item.getName().equals(s)){
            return item;
          }
        }
        return null;
      }
    });
    classTypeComboBox.setMaxWidth(Double.MAX_VALUE);
    GridPane.setHgrow(classTypeComboBox, Priority.ALWAYS);
    GridPane.setFillWidth(classTypeComboBox, true);

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

        if (!persistenceService.getOpenProject().getSourceFiles().contains(creationFolder.getPath() + "\\" + fileNameField.getText() + extension)) {
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
    HBox.setHgrow(buttonAlignmentRegion, Priority.ALWAYS);
    buttonLayout.setPadding(new Insets(10, 0, 10, 0));

    fieldLayout.add(fileNameLabel, 0, 0);
    fieldLayout.add(fileNameField, 1, 0);
    fieldLayout.add(fileExtensionComboBox, 2, 0);
    fieldLayout.add(classTypeLabel, 0, 1);
    fieldLayout.add(classTypeComboBox, 1, 1);
    fieldLayout.setHgap(10);
    fieldLayout.setVgap(10);

    windowLayout.setPadding(new Insets(10));

    initModality(Modality.APPLICATION_MODAL);
    setResizable(false);
  }

  private void onLoad(FolderDropdownItem creationFolder) {
    this.creationFolder = creationFolder;
    fileNameField.setText("");
    classTypeComboBox.setVisible(true);
    classTypeLabel.setVisible(true);
    fileExtensionComboBox.setValue(FileExtensionEnum.JAVA);
    classTypeComboBox.setValue(JavaClassTypesEnum.CLASS);
  }

  public void showWindow(FolderDropdownItem creationFolder) {
    onLoad(creationFolder);
    setScene(scene);
    show();
  }
}
