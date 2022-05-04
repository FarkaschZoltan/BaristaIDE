package com.farkasch.barista.gui.codinginterface;

import com.farkasch.barista.gui.component.ErrorPopup;
import com.farkasch.barista.services.FileService;
import com.farkasch.barista.services.JavaScriptService;
import com.farkasch.barista.services.PersistenceService;
import com.farkasch.barista.util.Result;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Scanner;
import java.util.stream.Collectors;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class CodingInterface extends BorderPane {

  @Autowired
  private JavaScriptService javaScriptService;
  @Autowired
  private PersistenceService persistenceService;
  @Autowired
  private FileService fileService;
  @Autowired
  private ApplicationContext applicationContext;
  @Autowired
  private ErrorPopup errorPopup;

  private CodingInterfaceContainer parent;
  private SwitchMenu switchMenu;
  private boolean interfaceLoaded = false;
  private WebView content;

  public void setParent(CodingInterfaceContainer parent){
    this.parent = parent;
  }

  public SwitchMenu getSwitchMenu(){
    return switchMenu;
  }

  public WebView getContentWebView(){
    return content;
  }

  @PostConstruct
  private void init(){
    content = new WebView();
    switchMenu = applicationContext.getBean(SwitchMenu.class);
    switchMenu.setParent(this);
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
    } else {
      switchMenu.switchToFile(file);
    }

    try {
      Scanner contentScanner = new Scanner(file);
      String textContent = new String();
      while (contentScanner.hasNextLine()) {
        String line = contentScanner.nextLine();
        System.out.println(line);
        textContent = textContent.concat(line + "\n");
      }
      contentScanner.close();
      javaScriptService.setContent(content, textContent, !interfaceLoaded);
      persistenceService.setActiveInterface(this);
      interfaceLoaded = true;
    } catch (FileNotFoundException e) {
      StringWriter stringWriter = new StringWriter();
      PrintWriter printWriter = new PrintWriter(stringWriter);
      e.printStackTrace(printWriter);
      File errorFile = fileService.createErrorLog(stringWriter.toString());
      errorPopup.showWindow(Result.ERROR("Error while opening file!", errorFile));

      printWriter.close();
      e.printStackTrace();
    }
  }

  public void close() {
    parent.closeInterface(this);
    persistenceService.setActiveInterface(null);
  }
}
