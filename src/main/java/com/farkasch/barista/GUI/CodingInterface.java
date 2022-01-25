package com.farkasch.barista.GUI;

import com.farkasch.barista.GUI.menu.CodingInterfaceSwitchMenu;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;

public class CodingInterface extends BorderPane {
    private TextArea content;
    private CodingInterfaceSwitchMenu switchMenu;

    public CodingInterface(){
        content = new TextArea();
        switchMenu = new CodingInterfaceSwitchMenu();
    }

    public CodingInterface(File file) {
        content = new TextArea();
        switchMenu = new CodingInterfaceSwitchMenu();

        showFile(file);
    }

    public void showFile(File file){

        try {
            Scanner contentScanner = new Scanner(file);
            StringBuilder contentBuilder = new StringBuilder();
            while(contentScanner.hasNextLine()){
                contentBuilder.append(contentScanner.nextLine());
            }
            content.setText(contentBuilder.toString());
        } catch(FileNotFoundException e){
            //TODO error popup!
            e.printStackTrace();
            System.out.println("Error while opening file!");
        }


        setTop(switchMenu);
        setCenter(content);
    }
}
