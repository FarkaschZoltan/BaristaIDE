package com.farkasch.barista.gui.mainview.sidemenu;

import com.farkasch.barista.gui.component.FolderDropdown.FolderDropdownItem;
import com.farkasch.barista.gui.component.WarningPopup;
import com.farkasch.barista.gui.mainview.MainStage;
import com.farkasch.barista.services.FileService;
import com.farkasch.barista.services.PersistenceService;
import java.nio.file.Paths;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public abstract class AbstractProjectPopup extends Stage {

  @Autowired
  protected FileService fileService;
  @Autowired
  protected PersistenceService persistenceService;
  @Autowired
  protected WarningPopup warningPopup;
  @Lazy
  @Autowired
  protected MainStage mainStage;

  protected TextField itemTextField;
  protected Label itemTextFieldLabel;
  protected Button applyButton;
  protected Region buttonAlignmentRegion;
  protected VBox windowLayout;
  protected GridPane fieldLayout;
  protected HBox buttonLayout;
  protected Scene scene;

  protected FolderDropdownItem item;

  @PostConstruct
  private void init(){
    itemTextField = new TextField();
    itemTextFieldLabel = new Label("New name: ");
    applyButton = new Button("Apply");
    buttonAlignmentRegion = new Region();
    buttonLayout = new HBox(buttonAlignmentRegion, applyButton);
    fieldLayout = new GridPane();
    windowLayout = new VBox(fieldLayout, buttonLayout);

    windowLayout.setPadding(new Insets(10));

    scene = new Scene(windowLayout, 300, 80);
    scene.getStylesheets().add("style.css");

    itemTextFieldLabel.setLabelFor(itemTextField);
    GridPane.setMargin(itemTextFieldLabel, new Insets(0, 10, 0, 0));
    GridPane.setHalignment(itemTextFieldLabel, HPos.LEFT);
    GridPane.setValignment(itemTextFieldLabel, VPos.CENTER);

    GridPane.setHgrow(itemTextField, Priority.ALWAYS);
    GridPane.setFillWidth(itemTextField, true);

    applyButton.setOnAction(click -> save());
    HBox.setHgrow(buttonAlignmentRegion, Priority.ALWAYS);

    buttonLayout.setPadding(new Insets(10, 0, 0, 0));

    fieldLayout.add(itemTextFieldLabel, 0, 0);
    fieldLayout.add(itemTextField, 1, 0);

    initModality(Modality.APPLICATION_MODAL);
    setResizable(false);
  }

  protected abstract void save();

  protected abstract void onLoad(FolderDropdownItem folderDropdownItem);

  public void showWindow(FolderDropdownItem folderDropdownItem){
    onLoad(folderDropdownItem);
    setScene(scene);
    show();
  }
}
