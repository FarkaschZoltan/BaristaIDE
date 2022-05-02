package com.farkasch.barista.gui.codinginterface;

import com.farkasch.barista.services.JavaScriptService;
import com.farkasch.barista.services.PersistenceService;
import java.io.File;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javax.annotation.PostConstruct;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class SwitchMenu extends HBox {

  @Autowired
  private PersistenceService persistenceService;

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
    persistenceService.refreshSideMenu();
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

  public void removeFile(int index) {
    persistenceService.removeOpenFile(((SwitchMenuItem)(getChildren().get(index))).getFile());
    persistenceService.addRecentlyClosed(((SwitchMenuItem)(getChildren().get(index))).getFile());
    persistenceService.refreshSideMenu();
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
    }

    private void initOpenButton() {
      openButton = new Button(file.getName());
      openButton.setOnAction(actionEvent -> {
        parent.showFile(file);
        currentlyActive.setContentId("switch-menu__item");
        currentlyActive = this;
        currentlyActive.setContentId("switch-menu__item--selected");
        persistenceService.setActiveFile(currentlyActive.getFile());
        persistenceService.setActiveInterface(parent);
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
        if (menu.getChildren().size() == 1) {
          menu.removeFile(index);
          parent.close();
        } else if (index > 0) {
          parent.showFile(
            ((SwitchMenuItem) menu.getChildren().get(index - 1)).getFile());
          menu.removeFile(index);
          currentlyActive = (SwitchMenuItem) menu.getChildren().get(index - 1);
          currentlyActive.setContentId("switch-menu__item--selected");
          persistenceService.setActiveFile(currentlyActive.getFile());
        } else {
          parent.showFile(
            ((SwitchMenuItem) menu.getChildren().get(index + 1)).getFile());
          menu.removeFile(index);
          currentlyActive = (SwitchMenuItem) menu.getChildren().get(index + 1);
          currentlyActive.setContentId("switch-menu__item--selected");
          persistenceService.setActiveFile(currentlyActive.getFile());
        }
      });
      closeButton.setGraphic(new FontIcon("mdi-close"));
      closeButton.setMinHeight(getHeight());
      getChildren().add(closeButton);
      closeButton.setId("switch-menu__item--selected");
    }

  }
}
