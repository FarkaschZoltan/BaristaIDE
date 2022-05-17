package com.farkasch.barista.gui.component;

import com.farkasch.barista.services.FileService;
import com.farkasch.barista.util.BaristaDragBoard;
import com.farkasch.barista.util.Result;
import com.farkasch.barista.util.TreeNode;
import com.farkasch.barista.util.enums.ResultTypeEnum;
import java.io.File;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.lang.Nullable;

public class FolderDropdown extends GridPane {

  private WarningPopup warningPopup;
  private FileService fileService;
  private BaristaDragBoard dragBoard;
  private double width; //for maintaining optimal width
  private Consumer<FolderDropdownItem> folderLeftClickAction;
  private Consumer<FolderDropdownItem> fileLeftClickAction;
  private Consumer<FolderDropdownItem> absoluteParentLeftClickAction;
  private List<MenuItem> folderContextMenuItems;
  private List<MenuItem> fileContextMenuItems;
  private List<MenuItem> absoluteParentContextMenuItems;
  private FolderDropdownItem lastClicked;
  private boolean showFiles;
  private boolean withAbsoluteParent;
  private boolean defaultFolderLeftClickAction = true;
  private boolean defaultFolderRightClickAction = true;
  private boolean defaultFileLeftClickAction = true;
  private boolean defaultFileRightClickAction = true;
  private boolean defaultAbsoluteParentRightClickAction = true;
  private boolean defaultAbsoluteParentLeftClickAction = true;
  private boolean dragAndDropEnabled = false;
  private TreeNode<FolderDropdownItem> rootNode; //for tracking the depth of the dropdown. Used for width calculations
  private ColumnConstraints columnConstraints;
  private HashMap<String, String> styleIds;
  private ContextMenu activeContextMenu; //for handling multiple left-clicks on the same item

  public FolderDropdown(double width, FileService fileService, WarningPopup warningPopup, HashMap<String, String> styleIds, boolean showFiles, boolean withAbsoluteParent) {
    this.width = width;
    this.fileService = fileService;
    this.warningPopup = warningPopup;
    this.showFiles = showFiles;
    this.withAbsoluteParent = withAbsoluteParent;
    this.styleIds = styleIds;

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

  public void setAbsoluteParentClickAction(
    Consumer<FolderDropdownItem> absoluteParentClickAction) {
    this.absoluteParentLeftClickAction = absoluteParentClickAction;
    defaultAbsoluteParentLeftClickAction = false;
  }

  public void setFileContextMenuItems(List<MenuItem> fileContextMenuItems) {
    this.fileContextMenuItems = fileContextMenuItems;
    defaultFileRightClickAction = false;
  }

  public void setFolderContextMenuItems(List<MenuItem> folderContextMenuItems) {
    this.folderContextMenuItems = folderContextMenuItems;
    defaultFolderRightClickAction = false;
  }

  public void setAbsoluteParentContextMenuItems(List<MenuItem> absoluteParentContextMenuItems) {
    this.absoluteParentContextMenuItems = absoluteParentContextMenuItems;
    defaultAbsoluteParentRightClickAction = false;
  }

  public void setDragBoard(BaristaDragBoard dragBoard) {
    this.dragBoard = dragBoard;
    this.dragAndDropEnabled = true;
  }

  public TreeNode<FolderDropdownItem> getRootNode() {
    return rootNode;
  }

  public FolderDropdownItem getLastClicked(){
    return lastClicked;
  }

  public void prepare(@Nullable String parentPath, @Nullable VBox parentContainer) {
    if (withAbsoluteParent && parentPath != null) {
      VBox absoluteParentContainer = new VBox();
      String absoluteParentName = parentPath.split("\\\\")[parentPath.split("\\\\").length - 1];
      FolderDropdownItem absoluteParent = new FolderDropdownItem(absoluteParentName, new File(parentPath).getParent(), parentContainer, rootNode);
      absoluteParent.setItemContainer(absoluteParentContainer);

      rootNode.setValue(absoluteParent);

      absoluteParentContainer.getChildren().add(absoluteParent);
      absoluteParentContainer.setMinWidth(width);
      absoluteParentContainer.setMaxWidth(Double.MAX_VALUE);

      FontIcon graphic = new FontIcon("mdi-folder");
      graphic.setId(styleIds.get("graphic"));
      absoluteParent.setGraphic(graphic);
      absoluteParent.setId(styleIds.get("item"));
      absoluteParent.setMaxWidth(Double.MAX_VALUE);

      absoluteParent.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> absoluteParent.getGraphic().setId(styleIds.get("graphic") + ":hover"));
      absoluteParent.addEventHandler(MouseEvent.MOUSE_EXITED, event -> absoluteParent.getGraphic().setId(styleIds.get("graphic")));

      absoluteParent.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
        if (event.getButton() == MouseButton.PRIMARY) {
          if (!defaultAbsoluteParentLeftClickAction) {
            absoluteParentLeftClickAction.accept(absoluteParent);
          }
          if (absoluteParentContainer.getChildren().size() > 1) {
            folderClose(absoluteParent.getItemContainer(), absoluteParent.getNode());
          } else {
            folderExpand(absoluteParent.getPath(), absoluteParent.getItemContainer(), absoluteParent.getNode());
          }
        } else if (event.getButton() == MouseButton.SECONDARY) {
          if (!defaultAbsoluteParentRightClickAction) {
            if (activeContextMenu != null) {
              activeContextMenu.hide();
            }
            activeContextMenu = createContextMenu(absoluteParentContextMenuItems);
            activeContextMenu.show(absoluteParent, event.getScreenX(), event.getScreenY());
          }
        }
      });
      addRow(0, absoluteParentContainer);
    } else {
      folderExpand(parentPath, parentContainer, rootNode);
    }
  }

  private void folderExpand(@Nullable String parentPath, @Nullable VBox parentContainer, TreeNode parentNode) {
    List<File> dirs = fileService.getDirsAndFiles(parentPath);
    GridPane folderSelector;
    if (parentContainer == null) {
      folderSelector = this;
    } else {
      folderSelector = new GridPane();
    }

    for (int i = 0; i < dirs.size(); i++) {
      TreeNode<FolderDropdownItem> node = new TreeNode<>();
      VBox folderContainer = new VBox();
      folderContainer.setMinWidth(width);
      FolderDropdownItem folderDropdownItem = new FolderDropdownItem(dirs.get(i).getName(), parentPath, parentContainer, folderContainer,
        folderSelector, node);
      node.setParent(parentNode);
      node.setValue(folderDropdownItem);
      Boolean isFile = dirs.get(i).isFile();
      folderDropdownItem.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> folderDropdownItem.getGraphic().setId(styleIds.get("graphic") + ":hover"));
      folderDropdownItem.addEventHandler(MouseEvent.MOUSE_EXITED, event -> folderDropdownItem.getGraphic().setId(styleIds.get("graphic")));
      if (isFile) {
        FontIcon graphic = new FontIcon("mdi-file");
        graphic.setId(styleIds.get("graphic"));
        folderDropdownItem.setGraphic(graphic);
      } else {
        FontIcon graphic = new FontIcon("mdi-folder");
        graphic.setId(styleIds.get("graphic"));
        folderDropdownItem.setGraphic(graphic);
      }

      enableDragAndDrop(folderDropdownItem);
      folderDropdownItem.setOnMouseClicked(getFolderDropdownItemMouseClick(folderDropdownItem, isFile, node));
      folderDropdownItem.setId(styleIds.get("item"));
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

    updateWidth();

    if (parentContainer != null) {
      parentContainer.getChildren().add(folderSelector);
    }
  }

  public void addFolderDropdownItem(FolderDropdownItem parentFolder, File file) {
    if (parentFolder.getItemContainer().getChildren().size() < 2) {
      folderExpand(parentFolder.getParentPath() == null ? System.getProperty("user.home") : parentFolder.getPath(), parentFolder.getItemContainer(),
        parentFolder.getNode());
      return;
    }
    GridPane childGrid = ((GridPane) (parentFolder.getItemContainer().getChildren().get(1)));
    VBox folderContainer = new VBox();
    TreeNode<FolderDropdownItem> node = new TreeNode<>();
    folderContainer.setMinWidth(width);
    FolderDropdownItem newItem = new FolderDropdownItem(file.getName(), file.getParent(), parentFolder.getItemContainer(), folderContainer, childGrid,
      node);
    node.setParent(parentFolder.getNode());
    node.setValue(newItem);
    folderContainer.getChildren().add(newItem);
    enableDragAndDrop(newItem);
    newItem.setOnMouseClicked(getFolderDropdownItemMouseClick(newItem, file.isFile(), parentFolder.getNode()));
    newItem.setId(styleIds.get("item"));
    newItem.setMaxWidth(Double.MAX_VALUE);
    newItem.setMaxHeight(Double.MAX_VALUE);
    newItem.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> newItem.getGraphic().setId(styleIds.get("graphic") + ":hover"));
    newItem.addEventHandler(MouseEvent.MOUSE_EXITED, event -> newItem.getGraphic().setId(styleIds.get("graphic")));
    if (file.isFile()) {
      FontIcon graphic = new FontIcon("mdi-file");
      graphic.setId(styleIds.get("graphic"));
      newItem.setGraphic(graphic);
    } else {
      FontIcon graphic = new FontIcon("mdi-folder");
      graphic.setId(styleIds.get("graphic"));
      newItem.setGraphic(graphic);
    }

    Comparator<Node> comparator = (a, b) -> {
      String text1 = ((FolderDropdownItem) (((VBox) a).getChildren().get(0))).getText().toLowerCase();
      String text2 = ((FolderDropdownItem) (((VBox) b).getChildren().get(0))).getText().toLowerCase();
      return text1.compareTo(text2);
    };

    int i = 0;
    int newItemIndex = 0;
    boolean foundIndex = false;
    for (Node child : childGrid.getChildren().sorted(comparator)) {
      if (((FolderDropdownItem) ((VBox) child).getChildren().get(0)).getText().toLowerCase().compareTo(newItem.getText().toLowerCase()) > 0) {
        if (!foundIndex) {
          newItemIndex = i;
          foundIndex = true;
        }
        GridPane.setRowIndex(child, i + 1);
      }
      i++;
    }

    if (!foundIndex) {
      newItemIndex = childGrid.getRowCount();
    }
    childGrid.addRow(newItemIndex, folderContainer);
  }

  public void removeFolderDropdownItem(FolderDropdownItem folderDropdownItem) {

    if (new File(folderDropdownItem.getPath()).isDirectory()) {
      folderClose(folderDropdownItem.getItemContainer(), folderDropdownItem.getNode());
    }
    GridPane grid = folderDropdownItem.getParentGrid();
    VBox nodeToRemove = new VBox();
    boolean removed = false;

    Comparator<Node> comparator = (a, b) -> {
      String text1 = ((FolderDropdownItem) (((VBox) a).getChildren().get(0))).getText().toLowerCase();
      String text2 = ((FolderDropdownItem) (((VBox) b).getChildren().get(0))).getText().toLowerCase();
      return text1.compareTo(text2);
    };

    for (Node node : grid.getChildren().sorted(comparator)) {
      VBox itemContainer = (VBox) node;
      if (removed) {
        GridPane.setRowIndex(itemContainer, GridPane.getRowIndex(itemContainer) - 1);
      }
      if (itemContainer.equals(folderDropdownItem.getItemContainer())) {
        removed = true;
        nodeToRemove = itemContainer;
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
            folderExpand((folderDropdownItem.getParentPath() == null ? System.getProperty("user.home") + "\\" + folderDropdownItem.getText() : folderDropdownItem.getPath()),
              folderDropdownItem.getItemContainer(), node);
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
          if (activeContextMenu != null) {
            activeContextMenu.hide();
          }
          activeContextMenu = createContextMenu(folderContextMenuItems);
          activeContextMenu.show(folderDropdownItem, click.getScreenX(), click.getScreenY());
        } else if (isFile && !defaultFileRightClickAction) {
          //file actions
          if (activeContextMenu != null) {
            activeContextMenu.hide();
          }
          activeContextMenu = createContextMenu(fileContextMenuItems);
          activeContextMenu.show(folderDropdownItem, click.getScreenX(), click.getScreenY());
        }
      }
      lastClicked = folderDropdownItem;
    };
    return event;
  }

  private void enableDragAndDrop(FolderDropdownItem folderDropdownItem) {
    if (dragAndDropEnabled) {
      //Drag start, same for folder and file
      folderDropdownItem.setOnDragDetected(event -> {
        Dragboard db = folderDropdownItem.startDragAndDrop(TransferMode.MOVE);
        //This needs to be here, because JavaFX only starts a dragEvent, when there is something in the drag board
        //-------------------------------------------------------------
        ClipboardContent cc = new ClipboardContent();
        cc.putString("dummy");
        db.setContent(cc);
        //-------------------------------------------------------------
        dragBoard.setDraggedItem(folderDropdownItem);
        event.consume();
      });
      //Only accept the drag, if the dragged item is a FolderDropdownItem
      folderDropdownItem.setOnDragOver(event -> {
        if (event.getGestureSource() != folderDropdownItem && dragBoard.getDraggedItem().getClass().equals(FolderDropdownItem.class)) {
          FolderDropdownItem item = (FolderDropdownItem) dragBoard.getDraggedItem();
          if (new File(item.getPath()).isFile()) {
            event.acceptTransferModes(TransferMode.MOVE);
          } else if (!fileService.folderContains(new File(item.getPath()).isFile() ? item.getParentPath() : item.getPath(),
            folderDropdownItem.getPath()))
          //makes sure a folder can't be dragged "into itself"
          {
            event.acceptTransferModes(TransferMode.MOVE);
          }
        }
        event.consume();
      });
      //Drag Done, same for folder and file
      folderDropdownItem.setOnDragDone(event -> {
        if (event.getTransferMode() == TransferMode.MOVE) {
          removeFolderDropdownItem((FolderDropdownItem) dragBoard.getDraggedItem());
        }
        dragBoard.dragDone();
        event.consume();
      });
      if (new File(folderDropdownItem.getPath()).isFile()) {
        //We change the style of the parent folder
        folderDropdownItem.setOnDragEntered(event -> {
          FolderDropdownItem parentFolder = (FolderDropdownItem) folderDropdownItem.getNode().getParent().getValue();
          parentFolder.setId(styleIds.get("dragEntered"));
          event.consume();
        });
        //We change back the style of the parent folder
        folderDropdownItem.setOnDragExited(event -> {
          FolderDropdownItem parentFolder = (FolderDropdownItem) folderDropdownItem.getNode().getParent().getValue();
          parentFolder.setId(styleIds.get("item"));
        });
        //We add the dragged item to the grid
        folderDropdownItem.setOnDragDropped(event -> {
          if (dragBoard.getDraggedItem().getClass().equals(FolderDropdownItem.class)) {
            FolderDropdownItem parentFolder = (FolderDropdownItem) folderDropdownItem.getNode().getParent().getValue();
            FolderDropdownItem draggedItem = (FolderDropdownItem) dragBoard.getDraggedItem();
            File destination;
            Result result;
            if (new File(draggedItem.getPath()).isFile()) {
              result = fileService.moveFile(new File(draggedItem.getPath()), parentFolder.getPath());
            } else {
              result = fileService.moveFolder(new File(draggedItem.getPath()), new File(parentFolder.getPath()));
            }
            if (result.getResult() == ResultTypeEnum.OK) {
              destination = (File) result.getReturnValue();
              addFolderDropdownItem(parentFolder, destination);
              event.setDropCompleted(true);
            } else {
              warningPopup.showWindow(result);
              event.setDropCompleted(false);
            }
          } else {
            event.setDropCompleted(false);
          }
          event.consume();
        });
      } else {
        //We change the style of this folder
        folderDropdownItem.setOnDragEntered(event -> {
          if (event.getGestureSource() != folderDropdownItem && dragBoard.getDraggedItem().getClass().equals(FolderDropdownItem.class)) {
            folderDropdownItem.setId(styleIds.get("dragEntered"));
          }
          event.consume();
        });
        //We change the style back to normal
        folderDropdownItem.setOnDragExited(event -> {
          folderDropdownItem.setId(styleIds.get("item"));
        });
        //We add the dragged item to the grid
        folderDropdownItem.setOnDragDropped(event -> {
          if (dragBoard.getDraggedItem().getClass().equals(FolderDropdownItem.class)) {
            FolderDropdownItem draggedItem = (FolderDropdownItem) dragBoard.getDraggedItem();
            File destination = null;
            Result result;
            if (new File(draggedItem.getPath()).isFile()) {
              result = fileService.moveFile(new File(draggedItem.getPath()), folderDropdownItem.getPath());
            } else {
              result = fileService.moveFolder(new File(draggedItem.getPath()), new File(folderDropdownItem.getPath()));
            }
            if (result.getResult() == ResultTypeEnum.OK) {
              destination = (File) result.getReturnValue();
              addFolderDropdownItem(folderDropdownItem, destination);
              event.setDropCompleted(true);
              System.out.println("here");
            } else {
              warningPopup.showWindow(result);
              event.setDropCompleted(false);
            }
          } else {
            event.setDropCompleted(false);
          }
          event.consume();
        });
      }
    }
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
