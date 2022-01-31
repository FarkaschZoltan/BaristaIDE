package com.farkasch.barista.gui.codinginterface;

import com.farkasch.barista.services.FileService;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;

public class CodingInterface extends BorderPane {

    private TextArea content;
    private SwitchMenu switchMenu;

    public TextArea getContent() {
        return content;
    }

    public File getShownFile() {
        return switchMenu.getCurrentlyActive();
    }

    public CodingInterface() {
        content = new TextArea();
        switchMenu = new SwitchMenu(this);

        setTop(switchMenu);
        setCenter(content);
    }

    public void showFile(File file) {

        if (switchMenu.getCurrentlyActive() != null) {
            FileService.saveFile(switchMenu.getCurrentlyActive(), content.getText());
        }

        if (!switchMenu.contains(file)) {
            switchMenu.addFile(file);
        }

        try {
            Scanner contentScanner = new Scanner(file);
            System.out.println(file.getName());
            content.setText("");
            while (contentScanner.hasNextLine()) {
                content.appendText(contentScanner.nextLine() + "\n");
            }

        } catch (FileNotFoundException e) {
            //TODO error popup!
            e.printStackTrace();
            System.out.println("Error while opening file!");
        }
    }
}
