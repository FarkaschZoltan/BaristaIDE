package com.farkasch.barista.gui.mainview.topmenu;

import com.farkasch.barista.MainApp;
import com.farkasch.barista.services.FileService;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;


public class TopMenu extends MenuBar {

    private MainApp mainApp;

    private Menu fileMenu;
    private Menu settingsMenu;
    private Menu gitMenu;
    private Menu helpMenu;

    public TopMenu(MainApp mainApp) {
        this.mainApp = mainApp;

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
            NewFileWindow newFileWindow = new NewFileWindow(
                file -> mainApp.getCodeArea().openFile(file));
            newFileWindow.show();
        });

        MenuItem newProject = new MenuItem("New Project");

        MenuItem loadProject = new MenuItem("Load Project");

        MenuItem saveProject = new MenuItem("Save");
        saveProject.setOnAction(actionEvent -> {
            FileService.saveFile(mainApp.getCodeArea().getActiveInterface().getShownFile(),
                mainApp.getCodeArea().getActiveInterface().getContent().getText());
        });

        fileMenu.getItems().addAll(newFile, newProject, loadProject, saveProject);
    }

    private void initGitMenu() {
        settingsMenu = new Menu("Settings");
    }

    private void initSettingsMenu() {
        gitMenu = new Menu("Git");
    }

    private void initHelpMenu() {
        helpMenu = new Menu("Help");
    }
}
