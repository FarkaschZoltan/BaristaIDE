package com.farkasch.barista.gui.codinginterface;

import com.farkasch.barista.services.FileService;
import com.farkasch.barista.services.JavaScriptService;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CodingInterface extends BorderPane {

  @Autowired
  private JavaScriptService javaScriptService;
  @Autowired
  private FileService fileService;

  private SwitchMenu switchMenu;
  private CodingInterfaceContainer parent;
  private boolean interfaceLoaded = false;
  private WebView content;

  public CodingInterface(CodingInterfaceContainer parent){
    this.parent = parent;
  }

  @PostConstruct
  private void init(){
    content = new WebView();
    switchMenu = new SwitchMenu(this);
    content.getEngine()
      .load(this.getClass().getResource("/codinginterface/codearea.html")
        .toExternalForm());

    setTop(switchMenu);
    setCenter(content);
  }

  public String getTextContent() {
    return javaScriptService.getContent(content);
  }

  public File getShownFile() {
    return switchMenu.getCurrentlyActive();
  }

  public void showFile(File file) {

    if (switchMenu.getCurrentlyActive() != null) {
      fileService.saveFile(switchMenu.getCurrentlyActive(),
        javaScriptService.getContent(content));
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
      System.out.println(switchMenu.getCurrentlyActive() == null);
      javaScriptService.setContent(content, textContent, !interfaceLoaded);
      interfaceLoaded = true;
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
