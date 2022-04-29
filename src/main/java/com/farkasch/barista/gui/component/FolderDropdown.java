package com.farkasch.barista.gui.component;

import com.farkasch.barista.services.ProcessService;
import com.farkasch.barista.util.TreeNode;
import java.awt.event.MouseEvent;
import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.lang.Nullable;

public class FolderDropdown extends GridPane {

  private ProcessService processService;
  private double width; //for maintaining optimal width
  private FolderConsumer folderLeftClickAction;
  private FolderConsumer fileLeftClickAction;
  private List<MenuItem> folderContextMenuItems;
  private List<MenuItem> fileContextMenuItems;
  private boolean showFiles;
  private boolean withAbsoluteParent;
  private boolean defaultFolderLeftClickAction = true;
  private boolean defaultFolderRightClickAction = true;
  private boolean defaultFileLeftClickAction = true;
  private boolean defaultFileRightClickAction = true;
  private TreeNode<Integer> rootNode;
  private ColumnConstraints columnConstraints;

  public FolderDropdown(double width, ProcessService processService, boolean showFiles, boolean withAbsoluteParent) {
    this.width = width;
    this.processService = processService;
    this.showFiles = showFiles;
    this.withAbsoluteParent = withAbsoluteParent;

    rootNode = new TreeNode<>();

    columnConstraints = new ColumnConstraints();
    columnConstraints.setMinWidth(width);
    getColumnConstraints().add(columnConstraints);
  }

  public void setFolderLeftClickAction(FolderConsumer folderLeftClickAction) {
    this.folderLeftClickAction = folderLeftClickAction;
    defaultFolderLeftClickAction = false;
  }

  public void setFileLeftClickAction(FolderConsumer fileLeftClickAction) {
    this.fileLeftClickAction = fileLeftClickAction;
    defaultFileLeftClickAction = false;
  }

  public void setFileContextMenuItems(List<MenuItem> fileContextMenuItems){
    this.fileContextMenuItems = fileContextMenuItems;
    defaultFileRightClickAction = false;
  }

  public void setFolderContextMenuItems(List<MenuItem> folderContextMenuItems){
    this.folderContextMenuItems = folderContextMenuItems;
    defaultFolderRightClickAction = false;
  }

  public void prepare(@Nullable String parentName, @Nullable VBox parentContainer) {
    if (withAbsoluteParent && parentName != null) {
      FolderDropDownItem absoluteParent = new FolderDropDownItem();
      VBox absoluteParentContainer = new VBox(absoluteParent);
      String absoluteParentName = parentName.split("\\\\")[parentName.split("\\\\").length - 1];

      absoluteParentContainer.setMinWidth(width);
      absoluteParentContainer.setMaxWidth(Double.MAX_VALUE);

      absoluteParent.setText(absoluteParentName);
      absoluteParent.setGraphic(new FontIcon("mdi-folder"));
      absoluteParent.setId("folder");
      absoluteParent.setMaxWidth(Double.MAX_VALUE);
      absoluteParent.setMaxHeight(Double.MAX_VALUE);
      absoluteParent.setOnAction(mouseEvent -> {
        if (defaultFolderLeftClickAction) {
          if (absoluteParentContainer.getChildren().size() > 1) {
            folderClose(absoluteParentContainer, rootNode);
          } else {
            folderExpand(parentName, absoluteParentContainer, rootNode);
          }
        } else {
          folderLeftClickAction.accept(parentName, parentContainer, absoluteParent);
        }
      });
      addRow(0, absoluteParentContainer);
    } else {
      folderExpand(parentName, parentContainer, rootNode);
    }
  }

  private void folderExpand(@Nullable String parentName, @Nullable VBox parentContainer, TreeNode parentNode) {;
    List<Pair<String, Boolean>> dirs = processService.getDirsAndFiles(parentName);
    GridPane folderSelector;
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
      folderContainer.setMinWidth(width);
      FolderDropDownItem folderButton = new FolderDropDownItem(dirs.get(i).getKey(), parentName);
      Boolean isFile = dirs.get(i).getValue();
      if (isFile) {
        folderButton.setGraphic(new FontIcon("mdi-file"));
      } else {
        folderButton.setGraphic(new FontIcon("mdi-folder"));
      }

      folderButton.setOnAction(mouseEvent -> {
        FolderDropDownItem target = folderButton;
        VBox parent = (VBox) (target.getParent());
        if (parent.getChildren().size() > 1) {
          if(!defaultFolderLeftClickAction){
            folderLeftClickAction.accept(parentName, parentContainer, target);
          }
          folderClose(parent, node);
        } else if (!isFile.booleanValue()) {
          if (!defaultFolderLeftClickAction) {
            folderLeftClickAction.accept(parentName, parentContainer, target);
          }
          folderExpand((parentName == null ? System.getProperty("user.home") : parentName) + "\\" + target.getText(),
            folderContainer, node);
        } else {
          if (defaultFileLeftClickAction) {
            //TODO: default file click action
            System.out.println("Click!");
          } else {
            fileLeftClickAction.accept(parentName, parentContainer, target);
          }
        }
      });

      if(!defaultFolderRightClickAction && !isFile){
        ContextMenu contextMenu = new ContextMenu();
        for(MenuItem item : folderContextMenuItems){
          MenuItem itemClone = new MenuItem();
          itemClone.setText(item.getText());
          itemClone.setOnAction(item.getOnAction());
          contextMenu.getItems().add(itemClone);
        }
        folderButton.setOnContextMenuRequested(click -> {
          contextMenu.show(folderButton, click.getScreenX(), click.getScreenY());
        });
      } else if(!defaultFileRightClickAction && isFile) {
        ContextMenu contextMenu = new ContextMenu();
        for(MenuItem item : fileContextMenuItems){
          MenuItem itemClone = new MenuItem();
          itemClone.setText(item.getText());
          itemClone.setOnAction(item.getOnAction());
          contextMenu.getItems().add(itemClone);
        }
        folderButton.setOnContextMenuRequested(click -> {
          contextMenu.show(folderButton, click.getScreenX(), click.getScreenY());
        });
      }

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

  private void updateWidth(){
    getColumnConstraints().get(0).setMinWidth(width + rootNode.getHeight() * 10);
  }

  @FunctionalInterface
  public interface FolderConsumer {

    void accept(String parentName, VBox parentContainer, FolderDropDownItem target);
  }

  public class FolderDropDownItem extends Button {
    private String parentPath;

    public FolderDropDownItem(){
      super();
    }
    public FolderDropDownItem(String text, String parentPath){
      super(text);
      this.parentPath = parentPath;
    }
    public String getParentPath() {
      return parentPath;
    }

    public void setParentPath(String parentPath) {
      this.parentPath = parentPath;
    }

    public String getPath(){
      return parentPath + "\\" + getText();
    }
  }
}
