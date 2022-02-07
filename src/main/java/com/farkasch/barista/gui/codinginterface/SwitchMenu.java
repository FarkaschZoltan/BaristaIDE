package com.farkasch.barista.gui.codinginterface;

import java.io.File;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.stereotype.Component;

@Component
public class SwitchMenu extends HBox {

    private CodingInterface codingInterface;
    private SwitchMenuItem currentlyActive;

    public File getCurrentlyActive() {
        return currentlyActive == null ? null : currentlyActive.getFile();
    }

    public SwitchMenu(CodingInterface codingInterface) {
        this.codingInterface = codingInterface;
        init();
    }

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
    }

    public void removeFile(int index) {
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

    private class SwitchMenuItem extends HBox {

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

        public SwitchMenuItem(File file) {
            this.file = file;
            setFillHeight(true);
            initOpenButton();
            initCloseButton();
        }

        private void initOpenButton() {
            openButton = new Button(file.getName());
            openButton.setOnAction(actionEvent -> {
                codingInterface.showFile(file);
                currentlyActive.setContentId("switch-menu__item");
                currentlyActive = this;
                currentlyActive.setContentId("switch-menu__item--selected");
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
                    codingInterface.close();
                } else if (index > 0) {
                    codingInterface.showFile(
                        ((SwitchMenuItem) menu.getChildren().get(index - 1)).getFile());
                    menu.removeFile(index);
                    currentlyActive = (SwitchMenuItem) menu.getChildren().get(index - 1);
                    currentlyActive.setContentId("switch-menu__item--selected");
                } else {
                    codingInterface.showFile(
                        ((SwitchMenuItem) menu.getChildren().get(index + 1)).getFile());
                    menu.removeFile(index);
                    currentlyActive = (SwitchMenuItem) menu.getChildren().get(index + 1);
                    currentlyActive.setContentId("switch-menu__item--selected");
                }
            });
            closeButton.setGraphic(new FontIcon("mdi-close"));
            closeButton.setMinHeight(getHeight());
            getChildren().add(closeButton);
            closeButton.setId("switch-menu__item--selected");
        }

    }
}
