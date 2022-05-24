package com.farkasch.barista.gui.codinginterface;

import com.farkasch.barista.gui.mainview.sidemenu.SideMenu;
import com.farkasch.barista.services.FileService;
import com.farkasch.barista.services.PersistenceService;
import com.farkasch.barista.util.BaristaDragBoard;
import java.io.File;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javax.annotation.PostConstruct;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SwitchMenu extends HBox {

  @Autowired
  private PersistenceService persistenceService;
  @Autowired
  private BaristaDragBoard dragBoard;
  @Autowired
  private FileService fileService;
  @Lazy
  @Autowired
  private CodingInterfaceContainer codingInterfaceContainer;
  @Lazy
  @Autowired
  private SideMenu sideMenu;

  private CodingInterface parent;
  private SwitchMenuItem currentlyActive;

  public File getCurrentlyActive() {
    return currentlyActive == null ? null : currentlyActive.getFile();
  }

  public void setParent(CodingInterface parent){
    this.parent = parent;
  }

  @PostConstruct
  private void init() {
    setId("switch-menu");
  }

  public void addFile(File file) {
    SwitchMenuItem widget = new SwitchMenuItem(file);
    getChildren().add(widget);
    if (currentlyActive != null) {
      currentlyActive.setContentId("switch-menu__item");
    }
    currentlyActive = widget;
    persistenceService.setActiveFile(currentlyActive.getFile());
    if(persistenceService.getOpenProject() == null){
      sideMenu.refresh();
    }
  }

  public void switchToFile(File file){
    for(Node item : getChildren()){
      SwitchMenuItem switchMenuItem = (SwitchMenuItem) item;
      if(switchMenuItem.getFile().equals(file)){
        currentlyActive = switchMenuItem;
        currentlyActive.setContentId("switch-menu__item--selected");
        persistenceService.setActiveFile(currentlyActive.getFile());
      } else {
        switchMenuItem.setContentId("switch-menu__item");
      }
    }
  }

  private void removeFile(int index) {
    if(persistenceService.getOpenProject() == null){
      persistenceService.removeOpenFile(((SwitchMenuItem)(getChildren().get(index))).getFile());
      persistenceService.addRecentlyClosed(((SwitchMenuItem)(getChildren().get(index))).getFile());
      sideMenu.refresh();
    }
    getChildren().remove(index);
  }

  public boolean contains(File file) {
    for (Node n : getChildren()) {
      if (((SwitchMenuItem) n).getFile().equals(file)) {
        return true;
      }
    }
    return false;
  }

  public void closeFile(File file){
    for(Node node : getChildren()){
      SwitchMenuItem switchMenuItem = (SwitchMenuItem) node;
      if(switchMenuItem.getFile().equals(file)){
        switchMenuItem.closeButton.getOnAction().handle(new ActionEvent());
        return;
      }
    }
  }

  public class SwitchMenuItem extends HBox {

    private File file;
    private Button openButton;
    private Button closeButton;

    public File getFile() {
      return file;
    }

    public void setFile(File file) {
      this.file = file;
    }

    public void setContentId(String id) {
      openButton.setId(id);
      closeButton.setId(id);
    }

    public void setText(String text){
      openButton.setText(text);
    }

    public SwitchMenuItem(File file) {
      this.file = file;
      init();
    }

    private void init(){
      setFillHeight(true);
      initOpenButton();
      initCloseButton();

      addEventFilter(MouseEvent.DRAG_DETECTED, event -> {
        System.out.println("Drag detected!");
        Dragboard db = this.startDragAndDrop(TransferMode.MOVE);
        //This needs to be here, because JavaFX only starts a dragEvent, when there is something in the drag board
        //-------------------------------------------------------------
        ClipboardContent cc = new ClipboardContent();
        cc.putString("dummy");
        db.setContent(cc);
        //-------------------------------------------------------------
        dragBoard.setDraggedItem(this);
        codingInterfaceContainer.prepareDrag();
        event.consume();
      });

      addEventFilter(DragEvent.DRAG_DONE, event -> {
        if(dragBoard.getDragTarget() != null && dragBoard.getDragTarget().getClass().equals(CodingInterface.class)){
          if(!dragBoard.getDragTarget().equals(parent)){
            this.closeButton.getOnAction().handle(new ActionEvent());
          }
        }
        dragBoard.dragDone();
        codingInterfaceContainer.endDrag();
        event.consume();
      });
    }

    private void initOpenButton() {
      openButton = new Button(file.getName());
      openButton.setOnAction(actionEvent -> {
        parent.showFileWithClick(file);
        currentlyActive.setContentId("switch-menu__item");
        currentlyActive = this;
        currentlyActive.setContentId("switch-menu__item--selected");
        persistenceService.setActiveFile(currentlyActive.getFile());
        codingInterfaceContainer.setActiveInterface(parent);
      });
      getChildren().add(openButton);
      openButton.setMinHeight(getHeight());
      openButton.setId("switch-menu__item--selected");
    }

    private void initCloseButton() {
      closeButton = new Button();
      closeButton.setOnAction(actionEvent -> {
        SwitchMenu menu = SwitchMenu.this;
        int index = menu.getChildren().indexOf(this);
        fileService.saveFile(file, parent.getTextContent());
        if (menu.getChildren().size() == 1) {
          persistenceService.setActiveFile(null);
          menu.removeFile(index);
          parent.close();
        } else if (index > 0) {
          parent.showFileWithClick(
            ((SwitchMenuItem) menu.getChildren().get(index - 1)).getFile());
          currentlyActive = (SwitchMenuItem) menu.getChildren().get(index - 1);
          currentlyActive.setContentId("switch-menu__item--selected");
          persistenceService.setActiveFile(currentlyActive.getFile());
          menu.removeFile(index);
        } else {
          parent.showFileWithClick(
            ((SwitchMenuItem) menu.getChildren().get(index + 1)).getFile());
          currentlyActive = (SwitchMenuItem) menu.getChildren().get(index + 1);
          currentlyActive.setContentId("switch-menu__item--selected");
          persistenceService.setActiveFile(currentlyActive.getFile());
          menu.removeFile(index);
        }
      });
      closeButton.setGraphic(new FontIcon("mdi-close"));
      closeButton.setMinHeight(getHeight());
      getChildren().add(closeButton);
      closeButton.setId("switch-menu__item--selected");
    }

  }
}
