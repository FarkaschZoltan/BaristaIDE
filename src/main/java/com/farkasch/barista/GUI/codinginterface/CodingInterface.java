package com.farkasch.barista.GUI.codinginterface;

import com.farkasch.barista.services.SaveService;
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

    public CodingInterface(){
        content = new TextArea();
        switchMenu = new SwitchMenu(this);

        setTop(switchMenu);
        setCenter(content);
    }

    public void showFile(File file){

        if(switchMenu.getCurrentlyActive() != null){
            SaveService.saveFile(file, content.getText());
        }

        if(!switchMenu.contains(file)){
            switchMenu.addFile(file);
        }

        try {
            StringBuilder contentBuilder = new StringBuilder();
            Scanner contentScanner = new Scanner(file);
            while(contentScanner.hasNextLine()){
                contentBuilder.append(contentScanner.nextLine());
            }
            content.setText(contentBuilder.toString());
        } catch(FileNotFoundException e){
            //TODO error popup!
            e.printStackTrace();
            System.out.println("Error while opening file!");
        }
    }
}
