package com.farkasch.barista.gui.component;

import com.farkasch.barista.services.ProcessService;
import com.farkasch.barista.util.TreeNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.lang.Nullable;

public class FolderDropdown extends GridPane {

  private ProcessService processService;
  private double buttonWidth; //for maintaining optimal width
  private double absoluteWidth;
  private FolderConsumer folderClickAction;
  private FolderConsumer fileClickAction;
  private boolean showFiles;
  private boolean withAbsoluteParent;
  private boolean defaultFolderClick = true;
  private boolean defaultFileClick = true;
  private ArrayList<Integer> widthList;
  private TreeNode<Integer> rootNode;
  private ColumnConstraints columnConstraints;

  public FolderDropdown(double buttonWidth, ProcessService processService, boolean showFiles, boolean withAbsoluteParent) {
    this.buttonWidth = buttonWidth;
    this.absoluteWidth = buttonWidth;
    this.processService = processService;
    this.showFiles = showFiles;
    this.withAbsoluteParent = withAbsoluteParent;

    widthList = new ArrayList<>();
    rootNode = new TreeNode<>();

    columnConstraints = new ColumnConstraints();
    columnConstraints.setMinWidth(absoluteWidth);

    getColumnConstraints().add(columnConstraints);
  }

  public void setFolderClickAction(FolderConsumer folderClickAction) {
    this.folderClickAction = folderClickAction;
    defaultFolderClick = false;
  }

  public void setFileClickAction(FolderConsumer fileClickAction) {
    this.fileClickAction = fileClickAction;
    defaultFileClick = false;
  }

  public void prepare(@Nullable String parentName, @Nullable VBox parentContainer) {
    if (withAbsoluteParent && parentName != null) {
      Button absoluteParent = new Button();
      VBox absoluteParentContainer = new VBox(absoluteParent);
      String absoluteParentName = parentName.split("\\\\")[parentName.split("\\\\").length - 1];

      absoluteParentContainer.setMinWidth(buttonWidth);
      absoluteParentContainer.setMaxWidth(Double.MAX_VALUE);

      absoluteParent.setText(absoluteParentName);
      absoluteParent.setGraphic(new FontIcon("mdi-folder"));
      absoluteParent.setId("folder");
      absoluteParent.setMaxWidth(Double.MAX_VALUE);
      absoluteParent.setMaxHeight(Double.MAX_VALUE);
      absoluteParent.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
        if (defaultFolderClick) {
          if (absoluteParentContainer.getChildren().size() > 1) {
            folderClose(absoluteParentContainer, rootNode);
          } else {
            folderExpand(parentName, absoluteParentContainer, rootNode);
          }
        } else {
          folderClickAction.accept(parentName, parentContainer, absoluteParent);
        }
      });
      addRow(0, absoluteParentContainer);
    } else {
      folderExpand(parentName, parentContainer, rootNode);
    }
  }

  private void folderExpand(@Nullable String parentName, @Nullable VBox parentContainer, TreeNode parentNode) {;
    List<Pair<String, Boolean>> dirs = processService.getDirsAndFiles(parentName);
    GridPane folderSelector = null;
    if (parentContainer == null) {
      folderSelector = this;
    } else {
      folderSelector = new GridPane();
    }

    TreeNode<Integer> node = new TreeNode<>();
    node.setParent(parentNode);
    updateWidth();

    for (int i = 0; i < dirs.size(); i++) {
      VBox folderContainer = new VBox();
      folderContainer.setMinWidth(buttonWidth);
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
          folderClose(parent, node);
        } else if (!isFile.booleanValue()) {
          if (!defaultFolderClick) {
            folderClickAction.accept(parentName, parentContainer, target);
          }
          folderExpand((parentName == null ? System.getProperty("user.home") : parentName) + "\\" + target.getText(),
            folderContainer, node);
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
      if (!isFile || showFiles) {
        folderContainer.getChildren().add(folderButton);
      }
      folderSelector.addRow(i, folderContainer);
      if (parentContainer != null) {
        folderSelector.setPadding(new Insets(0, 0, 0, 20));
      }
      folderSelector.setGridLinesVisible(true);
    }

    if (parentContainer != null) {
      parentContainer.getChildren().add(folderSelector);
    }
  }

  private void folderClose(VBox parent, TreeNode node) {
    parent.getChildren().remove(1, parent.getChildren().size());
    node.cutBelow();
    updateWidth();
  }

  //if add is true, then add to widthList, else remove;
  private void updateWidth(){
    getColumnConstraints().get(0).setMinWidth(buttonWidth + rootNode.getHeight() * 10);
  }

  @FunctionalInterface
  public interface FolderConsumer {

    void accept(String parentName, VBox parentContainer, Button target);
  }
}
