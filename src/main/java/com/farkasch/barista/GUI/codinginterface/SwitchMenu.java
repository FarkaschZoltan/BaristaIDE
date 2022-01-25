package com.farkasch.barista.GUI.codinginterface;

import com.farkasch.barista.services.SaveService;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import org.springframework.stereotype.Component;

@Component
public class SwitchMenu extends HBox {
    private CodingInterface codingInterface;
    private SwitchMenuItem currentlyActive;

    public File getCurrentlyActive() {
        return currentlyActive == null ? null : currentlyActive.getFile();
    }

    public SwitchMenu(CodingInterface codingInterface){
        this.codingInterface = codingInterface;
        init();
    }

    private void init(){
        setId("switch-menu");
    }

    public void addFile(File file){
        EventHandler<javafx.event.ActionEvent> clickEvent = actionEvent -> {
            if(currentlyActive != null){
                SaveService.saveFile(currentlyActive.getFile(), codingInterface.getContent().getText());
            }
            codingInterface.showFile(((SwitchMenuItem)actionEvent.getTarget()).getFile());
           for(Node n : getChildren()){
               if(n.equals(actionEvent.getTarget())){
                   currentlyActive.setId("switch-menu__item");
                   currentlyActive = (SwitchMenuItem) n;
                   currentlyActive.setId("switch-menu__item--selected");
                   break;
               }
           }
        };
        SwitchMenuItem widget = new SwitchMenuItem(file, clickEvent);
        getChildren().add(widget);
        if(currentlyActive != null){
            currentlyActive.setId("switch-menu__item");
        }
        widget.setId("switch-menu__item--selected");
        currentlyActive = widget;
    }

    public void removeFile(File file){
        getChildren().removeIf(m -> ((SwitchMenuItem)m).getFile().equals(file));
    }

    public boolean contains(File file) {
        for(Node n : getChildren()){
            if(((SwitchMenuItem)n).getFile().equals(file)){
                return true;
            }
        }
        return false;
    }

    public class SwitchMenuItem extends Button {
        private File file;

        public File getFile() {
            return file;
        }

        public void setFile(File file) {
            this.file = file;
        }

        public SwitchMenuItem(File file, EventHandler<ActionEvent> clickEvent){
            super(file.getName());
            setId("switch-menu__item");
            setOnAction(clickEvent);
            this.file = file;
        }

    }
}
