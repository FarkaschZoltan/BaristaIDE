package com.farkasch.barista.gui.component;

import com.farkasch.barista.services.PersistenceService;
import java.io.File;
import java.util.List;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class SimpleDropdown extends GridPane {

  private Button dropdownButton;

  private List<File> items;
  private String dropdownName;
  private PersistenceService persistenceService;
  private boolean open;

  public SimpleDropdown(String dropdownName, List<File> items, PersistenceService persistenceService) {
    this.dropdownName = dropdownName;
    this.items = items;
    this.persistenceService = persistenceService;

    dropdownButton = new Button(dropdownName);
    dropdownButton.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
      if (getChildren().size() > 1) {
        getChildren().remove(1, getChildren().size());
      } else {
        items.stream().forEach(file -> {
          Button openFileButton = new Button(file.getName());
          openFileButton.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent1 -> {
            persistenceService.getActiveInterface().showFile(file);
          });
          openFileButton.setId("side-menu__simple-dropdown--item");
          openFileButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
          addRow(getRowCount(), openFileButton);
        });
      }
    });
    dropdownButton.setId("side-menu__simple-dropdown--title");
    dropdownButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
    addRow(0, dropdownButton);

    ColumnConstraints cc = new ColumnConstraints();
    cc.setFillWidth(true);
    cc.setHgrow(Priority.ALWAYS);

    getColumnConstraints().add(cc);
  }

  public void refresh(List<File> items){
    this.items = items;
    if(getChildren().size() > 1){
      getChildren().remove(1, getChildren().size());
      items.stream().forEach(file -> {
        Button openFileButton = new Button(file.getName());
        openFileButton.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent1 -> {
          persistenceService.getActiveInterface().showFile(file);
        });
        openFileButton.setId("side-menu__simple-dropdown--item");
        openFileButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        addRow(getRowCount(), openFileButton);
      });
    }
  }
}
