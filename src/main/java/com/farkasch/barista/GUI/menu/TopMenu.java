package com.farkasch.barista.GUI.menu;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;

public class TopMenu extends MenuBar {
    private Menu fileMenu;
    private Menu settingsMenu;
    private Menu gitMenu;
    private Menu helpMenu;


    public TopMenu(){
        initFileMenu();
        initSettingsMenu();
        initGitMenu();
        initHelpMenu();

        getMenus().addAll(fileMenu, settingsMenu ,gitMenu, helpMenu);
    }

    private void initFileMenu(){
        fileMenu = new Menu("File");

        MenuItem newProject = new MenuItem("New Project");
        MenuItem loadProject = new MenuItem("Load Project");
        MenuItem saveProject = new MenuItem("Save");

        fileMenu.getItems().addAll(newProject, loadProject, saveProject);
    }

    private void initGitMenu(){
        settingsMenu = new Menu("Settings");
    }

    private void initSettingsMenu() {
        gitMenu = new Menu("Git");
    }

    private void initHelpMenu(){
        helpMenu = new Menu("Help");
    }
}
