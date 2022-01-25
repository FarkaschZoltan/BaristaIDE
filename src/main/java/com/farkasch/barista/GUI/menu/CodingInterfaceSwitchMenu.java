package com.farkasch.barista.GUI.menu;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;

public class CodingInterfaceSwitchMenu extends MenuBar {
    private List<File> files;

    public CodingInterfaceSwitchMenu(){
        files = new ArrayList<>();
    }

    public void addFile(File file){
        files.add(file);
        Menu menu = new Menu(file.getName());
        getMenus().add(menu);
    }

    public void removeFile(File file){
        files.remove(file);
        getMenus().removeIf(m -> m.getId().equals(file.getName()));
    }

    public boolean contains(File file) {
        return files.contains(file);
    }
}
