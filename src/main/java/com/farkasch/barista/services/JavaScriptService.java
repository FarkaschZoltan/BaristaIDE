package com.farkasch.barista.services;

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

  public String getContent(WebView view) {
    String script = "getContent()";
    String content = (String) view.getEngine().executeScript(script);
    return content;
  }

  public void setContent(WebView view, File fileToSet, boolean firstOpen) {
    persistenceService.setFileToOpen(fileToSet);
    System.out.println("file to set: " + persistenceService.getFileToOpen());
    String script = "loadContent()";
    if(firstOpen){
      open(view, script);
    } else {
      view.getEngine().executeScript(script);
    }
  }

  public void switchContent(WebView view, File fileToSet, boolean firstOpen) {
    persistenceService.setFileToSwitch(fileToSet);
    System.out.println("file to set: " + persistenceService.getFileToSwitch());
    String script = "switchContent()";
    if(firstOpen){
      open(view, script);
    } else {
      view.getEngine().executeScript(script);
    }
  }

  private void open(WebView view, String... scripts) throws JSException{
    view.getEngine().getLoadWorker().stateProperty().addListener(
      (observableValue, oldState, newState) -> {
        if (newState == State.SUCCEEDED) {
          JSObject win = (JSObject) view.getEngine().executeScript("window");
          win.setMember("persistenceService", persistenceService);
          for (String script : scripts) {
            System.out.println((String) view.getEngine().executeScript(script));
          }
        }
      }
    );
  }

}
