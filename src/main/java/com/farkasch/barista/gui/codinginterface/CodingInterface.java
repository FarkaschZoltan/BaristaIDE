package com.farkasch.barista.gui.codinginterface;

import com.farkasch.barista.services.FileService;
import com.farkasch.barista.services.JavaScriptService;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;

public class CodingInterface extends BorderPane {

    private WebView content;
    private SwitchMenu switchMenu;
    private CodingInterfaceContainer parent;

    public String getTextContent() {
        return JavaScriptService.getContent(content);
    }

    public File getShownFile() {
        return switchMenu.getCurrentlyActive();
    }

    public CodingInterface(CodingInterfaceContainer parent) {
        content = new WebView();
        content.getEngine()
            .load(this.getClass().getResource("/codinginterface/codearea.html")
                .toExternalForm());

        System.out.println(content.getEngine().getDocument());

        switchMenu = new SwitchMenu(this);
        this.parent = parent;

        setTop(switchMenu);
        setCenter(content);
    }

    public void showFile(File file) {

        if (switchMenu.getCurrentlyActive() != null) {
            FileService.saveFile(switchMenu.getCurrentlyActive(),
                JavaScriptService.getContent(content));
        }

        if (!switchMenu.contains(file)) {
            switchMenu.addFile(file);
        }

        try {
            Scanner contentScanner = new Scanner(file);
            System.out.println(file.getName());
            String textContent = new String();
            while (contentScanner.hasNextLine()) {
                textContent = textContent.concat(contentScanner.nextLine() + "\n");
            }
            System.out.println("textContent: " + textContent);
            System.out.println(content.getEngine());
            JavaScriptService.setContent(content, textContent);

        } catch (FileNotFoundException e) {
            //TODO error popup!
            e.printStackTrace();
            System.out.println("Error while opening file!");
        }
    }

    public void close() {
        parent.closeInterface(this);
    }
}
