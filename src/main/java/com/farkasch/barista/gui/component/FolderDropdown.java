package com.farkasch.barista.gui.component;

import com.farkasch.barista.services.FileService;
import com.farkasch.barista.services.ProcessService;
import com.farkasch.barista.util.TreeNode;
import java.io.File;
import java.util.List;
import java.util.function.Consumer;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.lang.Nullable;

public class FolderDropdown extends GridPane {

  private FileService fileService;
  private double width; //for maintaining optimal width
  private Consumer<FolderDropdownItem> folderLeftClickAction;
  private Consumer<FolderDropdownItem> fileLeftClickAction;
  private List<MenuItem> folderContextMenuItems;
  private List<MenuItem> fileContextMenuItems;
  private boolean showFiles;
  private boolean withAbsoluteParent;
  private boolean defaultFolderLeftClickAction = true;
  private boolean defaultFolderRightClickAction = true;
  private boolean defaultFileLeftClickAction = true;
  private boolean defaultFileRightClickAction = true;
  private TreeNode<FolderDropdownItem> rootNode; //for tracking the depth of the dropdown. Used for width calculations
  private ColumnConstraints columnConstraints;

  public FolderDropdown(double width, FileService fileService, boolean showFiles, boolean withAbsoluteParent) {
    this.width = width;
    this.fileService = fileService;
    this.showFiles = showFiles;
    this.withAbsoluteParent = withAbsoluteParent;

    rootNode = new TreeNode<>();

    columnConstraints = new ColumnConstraints();
    columnConstraints.setMinWidth(width);
    getColumnConstraints().add(columnConstraints);
  }

  public void setFolderLeftClickAction(Consumer<FolderDropdownItem> folderLeftClickAction) {
    this.folderLeftClickAction = folderLeftClickAction;
    defaultFolderLeftClickAction = false;
  }

  public void setFileLeftClickAction(Consumer<FolderDropdownItem> fileLeftClickAction) {
    this.fileLeftClickAction = fileLeftClickAction;
    defaultFileLeftClickAction = false;
  }

  public void setFileContextMenuItems(List<MenuItem> fileContextMenuItems) {
    this.fileContextMenuItems = fileContextMenuItems;
    defaultFileRightClickAction = false;
  }

  public void setFolderContextMenuItems(List<MenuItem> folderContextMenuItems) {
    this.folderContextMenuItems = folderContextMenuItems;
    defaultFolderRightClickAction = false;
  }

  public void prepare(@Nullable String parentPath, @Nullable VBox parentContainer) {
    if (withAbsoluteParent && parentPath != null) {
      VBox absoluteParentContainer = new VBox();
      String absoluteParentName = parentPath.split("\\\\")[parentPath.split("\\\\").length - 1];
      FolderDropdownItem absoluteParent = new FolderDropdownItem(absoluteParentName, parentPath, absoluteParentContainer, rootNode);
      absoluteParent.setParentContainer(absoluteParentContainer);

      rootNode.setValue(absoluteParent);

      absoluteParentContainer.getChildren().add(absoluteParent);
      absoluteParentContainer.setMinWidth(width);
      absoluteParentContainer.setMaxWidth(Double.MAX_VALUE);

      absoluteParent.setGraphic(new FontIcon("mdi-folder"));
      absoluteParent.setId("folder");
      absoluteParent.setMaxWidth(Double.MAX_VALUE);
      absoluteParent.setMaxHeight(Double.MAX_VALUE);
      absoluteParent.setOnAction(mouseEvent -> {
        if (!defaultFolderLeftClickAction) {
          folderLeftClickAction.accept(absoluteParent);
        }
        if (absoluteParentContainer.getChildren().size() > 1) {
          folderClose(absoluteParentContainer, rootNode);
        } else {
          folderExpand(parentPath, absoluteParentContainer, rootNode);
        }
      });
      addRow(0, absoluteParentContainer);
    } else {
      folderExpand(parentPath, parentContainer, rootNode);
    }
  }

  private void folderExpand(@Nullable String parentName, @Nullable VBox parentContainer, TreeNode parentNode) {
    List<File> dirs = fileService.getDirsAndFiles(parentName);
    GridPane folderSelector;
    if (parentContainer == null) {
      folderSelector = this;
    } else {
      folderSelector = new GridPane();
    }

    TreeNode<FolderDropdownItem> node = new TreeNode<>();
    node.setParent(parentNode);
    updateWidth();

    for (int i = 0; i < dirs.size(); i++) {
      VBox folderContainer = new VBox();
      folderContainer.setMinWidth(width);
      FolderDropdownItem folderDropdownItem = new FolderDropdownItem(dirs.get(i).getName(), parentName, parentContainer, folderContainer,
        folderSelector, node);
      node.setValue(folderDropdownItem);
      Boolean isFile = dirs.get(i).isFile();
      if (isFile) {
        folderDropdownItem.setGraphic(new FontIcon("mdi-file"));
      } else {
        folderDropdownItem.setGraphic(new FontIcon("mdi-folder"));
      }

      folderDropdownItem.setOnMouseClicked(getFolderDropdownItemMouseClick(folderDropdownItem, isFile, node));
      folderDropdownItem.setId("folder");
      folderDropdownItem.setMaxWidth(Double.MAX_VALUE);
      folderDropdownItem.setMaxHeight(Double.MAX_VALUE);
      if (!isFile || showFiles) {
        folderContainer.getChildren().add(folderDropdownItem);
      }
      folderSelector.addRow(i, folderContainer);
      if (parentContainer != null) {
        folderSelector.setPadding(new Insets(0, 0, 0, 20));
      }
    }

    if (parentContainer != null) {
      parentContainer.getChildren().add(folderSelector);
    }
  }

  public void addFolderDropdownItem(FolderDropdownItem folder, File file) {
    if (folder.getItemContainer().getChildren().size() < 2) {
      folderExpand(folder.getParentPath() == null ? System.getProperty("user.home") : folder.getPath(), folder.getItemContainer(), folder.getNode());
      return;
    }
    GridPane childGrid = ((GridPane) (folder.getItemContainer().getChildren().get(1)));
    VBox folderContainer = new VBox();
    folderContainer.setMinWidth(width);
    FolderDropdownItem newItem = new FolderDropdownItem(file.getName(), file.getParent(), folder.getItemContainer(), folderContainer, childGrid,
      folder.getNode());
    folderContainer.getChildren().add(newItem);
    newItem.setOnMouseClicked(getFolderDropdownItemMouseClick(newItem, file.isFile(), folder.getNode()));
    newItem.setId("folder");
    newItem.setMaxWidth(Double.MAX_VALUE);
    newItem.setMaxHeight(Double.MAX_VALUE);
    if (file.isFile()) {
      newItem.setGraphic(new FontIcon("mdi-file"));
    } else {
      newItem.setGraphic(new FontIcon("mdi-folder"));
    }

    int i = 0;
    int newItemIndex = 0;
    boolean foundIndex = false;
    for (Node node : childGrid.getChildren()) {
      if (((FolderDropdownItem) ((VBox) node).getChildren().get(0)).getText().toLowerCase().compareTo(newItem.getText().toLowerCase()) > 0) {
        if (!foundIndex) {
          newItemIndex = i;
          foundIndex = true;
          System.out.println("here!");
        }
        GridPane.setRowIndex(node, i + 1);
      }
      i++;
    }

    if (!foundIndex) {
      newItemIndex = childGrid.getRowCount();
    }
    childGrid.addRow(newItemIndex, folderContainer);
  }

  public void removeFolderDropdownItem(FolderDropdownItem folderDropdownItem){
    GridPane grid = folderDropdownItem.getParentGrid();
    VBox nodeToRemove = new VBox();
    boolean removed = false;
    for(Node node : grid.getChildren()){
      VBox itemContainer = (VBox) node;
      if(itemContainer.equals(folderDropdownItem.getItemContainer())){
        removed = true;
        nodeToRemove = itemContainer;
      }
      if(removed){
        GridPane.setRowIndex(itemContainer, GridPane.getRowIndex(itemContainer) - 1);
      }
    }
    rootNode.removeNode(folderDropdownItem.getNode());
    grid.getChildren().remove(nodeToRemove);
  }

  private void folderClose(VBox parent, TreeNode node) {
    parent.getChildren().remove(1, parent.getChildren().size());
    node.cutBelow();
    updateWidth();
  }

  private void updateWidth() {
    getColumnConstraints().get(0).setMinWidth(width + rootNode.getHeight() * 20);
  }

  private ContextMenu createContextMenu(List<MenuItem> menuItems) {
    ContextMenu contextMenu = new ContextMenu();
    for (MenuItem menuItem : menuItems) {
      MenuItem menuItemClone = new MenuItem();
      menuItemClone.setText(menuItem.getText());
      menuItemClone.setOnAction(menuItem.getOnAction());
      contextMenu.getItems().add(menuItemClone);
    }
    return contextMenu;
  }

  private EventHandler<? super MouseEvent> getFolderDropdownItemMouseClick(FolderDropdownItem folderDropdownItem, Boolean isFile,
    TreeNode<FolderDropdownItem> node) {
    EventHandler<? super MouseEvent> event = click -> {
      VBox itemContainer = folderDropdownItem.getItemContainer();
      if (click.getButton() == MouseButton.PRIMARY) {
        if (!isFile.booleanValue()) {
          //folder actions
          if (!defaultFolderLeftClickAction) {
            folderLeftClickAction.accept(folderDropdownItem);
          }
          if (itemContainer.getChildren().size() > 1) {
            folderClose(itemContainer, node);
          } else {
            folderExpand((folderDropdownItem.getParentPath() == null ? System.getProperty("user.home") : folderDropdownItem.getParentPath()) + "\\"
              + folderDropdownItem.getText(), folderDropdownItem.getItemContainer(), node);
          }
        } else {
          //file actions
          if (!defaultFileLeftClickAction) {
            fileLeftClickAction.accept(folderDropdownItem);
          }
        }
      } else if (click.getButton() == MouseButton.SECONDARY) {
        if (!isFile.booleanValue() && !defaultFolderRightClickAction) {
          //folder actions
          createContextMenu(folderContextMenuItems).show(folderDropdownItem, click.getScreenX(), click.getScreenY());
        } else if (isFile && !defaultFileRightClickAction) {
          //file actions
          createContextMenu(fileContextMenuItems).show(folderDropdownItem, click.getScreenX(), click.getScreenY());
        }
      }
    };
    return event;
  }

  public class FolderDropdownItem extends Button {

    private String parentPath; //Path of parent folder
    private VBox parentContainer; //Container of parent folder
    private VBox itemContainer; //Container of this item
    private GridPane parentGrid; // Grid that contains items in the same "depth"
    private TreeNode<FolderDropdownItem> node; //node representing this item in the directory tree

    public FolderDropdownItem() {
      super();
    }

    public FolderDropdownItem(String text, String parentPath, VBox parentContainer, TreeNode<FolderDropdownItem> node) {
      super(text);
      this.parentPath = parentPath;
      this.parentContainer = parentContainer;
      this.node = node;
    }

    public FolderDropdownItem(String text, String parentPath, VBox parentContainer, VBox itemContainer, GridPane parentGrid,
      TreeNode<FolderDropdownItem> node) {
      this(text, parentPath, parentContainer, node);
      this.itemContainer = itemContainer;
      this.parentGrid = parentGrid;
    }

    public String getParentPath() {
      return parentPath;
    }

    public void setParentPath(String parentPath) {
      this.parentPath = parentPath;
    }

    public VBox getParentContainer() {
      return parentContainer;
    }

    public void setParentContainer(VBox parentContainer) {
      this.parentContainer = parentContainer;
    }

    public TreeNode<FolderDropdownItem> getNode() {
      return node;
    }

    public void setNode(TreeNode<FolderDropdownItem> node) {
      this.node = node;
    }

    public VBox getItemContainer() {
      return itemContainer;
    }

    public void setItemContainer(VBox itemContainer) {
      this.itemContainer = itemContainer;
    }

    public GridPane getParentGrid() {
      return parentGrid;
    }

    public void setParentGrid(GridPane parentGrid) {
      this.parentGrid = parentGrid;
    }

    public String getPath() {
      return parentPath + "\\" + getText();
    }
  }
}
