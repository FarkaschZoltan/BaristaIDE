package com.farkasch.barista.services;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.scene.web.WebView;

public class JavaScriptService {

  public static String getContent(WebView view) {
    String script = "document.querySelector(\"#code-area\").textContent";
    return (String) view.getEngine().executeScript(script);
  }

  public static void setContent(WebView view, String content) {
    if (!content.equals("")) {
      System.out.println(content);
      String script = "(function(){"
        + "document.getElementById(\"code-area\").textContent = `" + content + "`;"
        + "})();";
      execute(view, script);
    }
  }

  private static void execute(WebView view, String... scripts){
    view.getEngine().getLoadWorker().stateProperty().addListener(
      (observableValue, oldState, newState) -> {
        if(newState == State.SUCCEEDED){
          for(String script : scripts){
            view.getEngine().executeScript(script);
          }
        }
      }
    );
  }

}
