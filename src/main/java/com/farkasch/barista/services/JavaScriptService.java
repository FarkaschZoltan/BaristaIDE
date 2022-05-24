package com.farkasch.barista.services;

import com.google.common.io.Files;
import java.io.File;
import javafx.concurrent.Worker.State;
import javafx.scene.web.WebView;
import netscape.javascript.JSException;
import netscape.javascript.JSObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class JavaScriptService {

  @Lazy
  @Autowired
  private PersistenceService persistenceService;
  @Lazy
  @Autowired
  private FileService fileService;

  public String getContent(WebView view) {
    String script = "getContent()";
    String content = (String) view.getEngine().executeScript(script);
    return content;
  }

  public void setContent(WebView view, File fileToSet, boolean firstOpen) {
    persistenceService.setFileToOpen(fileToSet);
    String openContent = "loadContent()";
    String modeScript;
    if(Files.getFileExtension(fileToSet.getAbsolutePath()).equals("java")){
      modeScript = "activateJavaMode()";
    } else {
      modeScript = "activateTextMode()";
    }
    if(firstOpen){
      open(view, openContent, modeScript);
    } else {
      view.getEngine().executeScript(openContent);
      view.getEngine().executeScript(modeScript);
    }
  }

  public void switchContent(WebView view, File fileToSet, boolean firstOpen) {
    persistenceService.setFileToSwitch(fileToSet);
    String switchContent = "switchContent()";
    String modeScript;
    if(Files.getFileExtension(fileToSet.getAbsolutePath()).equals("java")){
      modeScript = "activateJavaMode()";
    } else {
      modeScript = "activateTextMode()";
    }
    if(firstOpen){
      open(view, switchContent, modeScript);
    } else {
      view.getEngine().executeScript(switchContent);
      view.getEngine().executeScript(modeScript);
    }
  }

  public int getCursorLine(WebView view){
    String script = "getCursorLine()";
    return (int) view.getEngine().executeScript(script);
  }

  public void insertGeneratedContent(WebView view){
    String script = "insertGeneratedContent()";
    view.getEngine().executeScript(script);
  }

  public void activateJavaMode(WebView view){
    String script = "activateJavaMode()";
    view.getEngine().executeScript(script);
  }

  public void activateTextMode(WebView view){
    String script = "activateTextMode()";
    view.getEngine().executeScript(script);
  }

  private void open(WebView view, String... scripts) throws JSException{
    view.getEngine().getLoadWorker().stateProperty().addListener(
      (observableValue, oldState, newState) -> {
        if (newState == State.SUCCEEDED) {
          JSObject win = (JSObject) view.getEngine().executeScript("window");
          win.setMember("persistenceService", persistenceService);
          win.setMember("fileService", fileService);
          for (String script : scripts) {
            view.getEngine().executeScript(script);
          }
        }
      }
    );
  }
}
