package com.farkasch.barista.gui.component;

import com.farkasch.barista.services.ProcessService;
import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.lang.Nullable;

public class FolderDropdown extends GridPane {

  private ProcessService processService;
  private Scene scene;
  private FolderConsumer folderClickAction;
  private FolderConsumer fileClickAction;

  private boolean showFiles;
  private boolean defaultFolderClick = true;
  private boolean defaultFileClick = true;

  public FolderDropdown(Scene scene, ProcessService processService, boolean showFiles){
    this.scene = scene;
    this.processService = processService;
    this.showFiles = showFiles;
  }

  public void setScene(Scene scene) {
    this.scene = scene;
  }

  public void setFolderClickAction(FolderConsumer folderClickAction) {
    this.folderClickAction = folderClickAction;
    defaultFolderClick = false;
  }

  public void setFileClickAction(FolderConsumer fileClickAction) {
    this.fileClickAction = fileClickAction;
    defaultFileClick = false;
  }

  public void folderExpand(@Nullable String parentName, @Nullable VBox parentContainer) {
    List<Pair<String, Boolean>> dirs = processService.getDirsAndFiles(parentName);
    GridPane folderSelector = null;
    if (parentContainer == null) {
      folderSelector = this;
    } else {
      folderSelector = new GridPane();
    }

    for (int i = 0; i < dirs.size(); i++) {
      VBox folderContainer = new VBox();
      folderContainer.setMinWidth(
        parentContainer == null ? scene.getWidth() : parentContainer.getWidth());
      Button folderButton = new Button(dirs.get(i).getKey());
      Boolean isFile = dirs.get(i).getValue();
      if (isFile) {
        folderButton.setGraphic(new FontIcon("mdi-file"));
      } else {
        folderButton.setGraphic(new FontIcon("mdi-folder"));
      }
      folderButton.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
        Button target = folderButton;
        VBox parent = (VBox) (target.getParent());
        if (parent.getChildren().size() > 1) {
          folderClose(parent);
        } else if (!isFile.booleanValue()) {
          if (!defaultFolderClick) {
            folderClickAction.accept(parentName, parentContainer, target);
          }
          folderExpand((parentName == null ? "" : parentName) + "\\" + target.getText(),
            folderContainer);
        } else {
          if (defaultFileClick) {
            //TODO: default file click action
            System.out.println("Click!");
          } else {
            fileClickAction.accept(parentName, parentContainer, target);
          }
        }
      });

      folderButton.setId("folder");
      folderButton.setMaxWidth(Double.MAX_VALUE);
      folderButton.setMaxHeight(Double.MAX_VALUE);
      if(!isFile || showFiles){
        folderContainer.getChildren().add(folderButton);
      }
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
    parent.getChildren().remove(1, parent.getChildren().size());
  }

  @FunctionalInterface
  public interface FolderConsumer {

    void accept(String parentName, VBox parentContainer, Button target);
  }
}
