package com.farkasch.barista.gui.mainview.topmenu;

import com.farkasch.barista.gui.mainview.MainStage;
import com.farkasch.barista.gui.mainview.topmenu.settingswindow.CompileSettingsWindow;
import com.farkasch.barista.services.FileService;
import com.farkasch.barista.services.PersistenceService;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class TopMenu extends MenuBar {

  @Autowired
  private FileService fileService;
  @Autowired
  private CompileSettingsWindow compileSettingsWindow;
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
  @Lazy
  @Autowired
  private MainStage mainStage;

  private Menu fileMenu;
  private Menu settingsMenu;
  private Menu gitMenu;
  private Menu helpMenu;

  @PostConstruct
  private void init() {
    initFileMenu();
    initSettingsMenu();
    initGitMenu();
    initHelpMenu();

    getMenus().addAll(fileMenu, settingsMenu, gitMenu, helpMenu);
  }

  private void initFileMenu() {
    fileMenu = new Menu("File");

    MenuItem newFile = new MenuItem("New File");
    newFile.setOnAction(actionEvent -> {
      newFileWindow.showWindow(file -> {
        mainStage.openNewFile(file);
        persistenceService.addOpenFile(file);
      });
    });

    MenuItem newProject = new MenuItem("New Project");
    newProject.setOnAction(actionEvent -> {
      newProjectWindow.showWindow();
    });

    MenuItem openFile = new MenuItem("Open File");
    openFile.setOnAction(actionEvent -> {
      openFileWindow.showWindow(file -> {
        mainStage.openNewFile(file);
        persistenceService.addOpenFile(file);
      });
    });

    MenuItem loadProject = new MenuItem("Load Project");
    loadProject.setOnAction(actionEvent -> {
      loadProjectWindow.showWindow();
    });

    MenuItem saveProject = new MenuItem("Save");
    saveProject.setOnAction(actionEvent -> {
      fileService.saveFile(persistenceService.getActiveInterface().getShownFile(),
        persistenceService.getActiveInterface().getTextContent());
    });

    fileMenu.getItems().addAll(newFile, newProject, openFile, loadProject, saveProject);
  }

  private void initGitMenu() {
    gitMenu = new Menu("Git");
  }

  private void initSettingsMenu() {

    settingsMenu = new Menu("Settings");
    MenuItem compileSettings = new MenuItem("Compile");
    compileSettings.setOnAction(actionEvent -> {
      compileSettingsWindow.showWindow();
    });

    settingsMenu.getItems().addAll(compileSettings);
  }

  private void initHelpMenu() {
    helpMenu = new Menu("Help");
  }
}
