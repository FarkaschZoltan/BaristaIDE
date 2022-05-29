package com.farkasch.barista.gui.mainview.topmenu;

import com.farkasch.barista.gui.codinginterface.CodingInterfaceContainer;
import com.farkasch.barista.services.FileService;
import com.farkasch.barista.services.PersistenceService;
import com.farkasch.barista.services.ProcessService;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TopMenu extends MenuBar {

  @Autowired
  private FileService fileService;
  @Autowired
  private ProcessService processService;
  @Autowired
  private NewFileWindow newFileWindow;
  @Autowired
  private OpenFileWindow openFileWindow;
  @Autowired
  private NewProjectWindow newProjectWindow;
  @Autowired
  private LoadProjectWindow loadProjectWindow;
  @Autowired
  private PersistenceService persistenceService;
  @Autowired
  private CodingInterfaceContainer codingInterfaceContainer;
  private Menu fileMenu;
  private Menu helpMenu;

  @PostConstruct
  private void init() {
    initFileMenu();
    initHelpMenu();

    getMenus().addAll(fileMenu, helpMenu);
  }

  private void initFileMenu() {
    fileMenu = new Menu("File");

    MenuItem newFile = new MenuItem("New File");
    newFile.setOnAction(actionEvent -> {
      newFileWindow.showWindow(file -> {
        persistenceService.addOpenFile(file);
        codingInterfaceContainer.openFile(file);
      });
    });

    MenuItem newProject = new MenuItem("New Project");
    newProject.setOnAction(actionEvent -> {
      newProjectWindow.showWindow();
    });

    MenuItem openFile = new MenuItem("Open File");
    openFile.setOnAction(actionEvent -> {
      openFileWindow.showWindow(file -> {
        persistenceService.addOpenFile(file);
        codingInterfaceContainer.openFile(file);
      });
    });

    MenuItem loadProject = new MenuItem("Load Project");
    loadProject.setOnAction(actionEvent -> {
      loadProjectWindow.showWindow();
    });

    MenuItem saveProject = new MenuItem("Save");
    saveProject.setOnAction(actionEvent -> {
      codingInterfaceContainer.getInterfaces().stream()
        .forEach(codingInterface -> fileService.saveFile(codingInterface.getShownFile(), codingInterface.getTextContent()));
    });

    fileMenu.getItems().addAll(newFile, newProject, openFile, loadProject, saveProject);
  }
  private void initHelpMenu() {
    helpMenu = new Menu("Help");

    MenuItem documentation = new MenuItem("Documentation");
    documentation.setOnAction(click -> {
      Thread thread = new Thread(() -> {
        processService.openDocumentation();
      });
      thread.start();
    });

    helpMenu.getItems().add(documentation);
  }
}
