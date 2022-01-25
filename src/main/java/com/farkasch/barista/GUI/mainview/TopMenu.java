package com.farkasch.barista.GUI.mainview;

import com.farkasch.barista.MainApp;
import java.io.File;
import java.io.IOException;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;


public class TopMenu extends MenuBar {

    private MainApp mainApp;

    private Menu fileMenu;
    private Menu settingsMenu;
    private Menu gitMenu;
    private Menu helpMenu;

    public TopMenu(MainApp mainApp){
        this.mainApp = mainApp;

        initFileMenu();
        initSettingsMenu();
        initGitMenu();
        initHelpMenu();

        getMenus().addAll(fileMenu, settingsMenu ,gitMenu, helpMenu);
    }

    private void initFileMenu(){
        fileMenu = new Menu("File");

        MenuItem newFile = new MenuItem("New File");
        newFile.setOnAction(action -> {
            File f = new File("C:\\Users\\farka\\Documents\\teszt\\teszt" + (int)(Math.random() * (100 - 0 + 1) + 0) + ".txt");
            if(!f.exists()){
                try{
                    f.createNewFile();
                } catch(IOException e){
                    System.out.println("Error while creating file!");
                    e.printStackTrace();
                }
            }
            mainApp.getCodeArea().openFile(0, f);
        });
        MenuItem newProject = new MenuItem("New Project");
        MenuItem loadProject = new MenuItem("Load Project");
        MenuItem saveProject = new MenuItem("Save");

        fileMenu.getItems().addAll(newFile, newProject, loadProject, saveProject);
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
