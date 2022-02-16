package com.farkasch.barista.services;

import javafx.concurrent.Worker.State;
import javafx.scene.web.WebView;

public class JavaScriptService {

  public static String getContent(WebView view) {
    String script = "document.querySelector(\"#code-area\").textContent";
    return (String) view.getEngine().executeScript(script);
  }

  public static void setContent(WebView view, String content, boolean firstOpen) {
    if (!content.equals("")) {
      System.out.println(content);
      String script = "(function(){"
        + "document.getElementById(\"code-area\").textContent = `" + content + "`;"
        + "})();";
      if(firstOpen){
        open(view, script, "highlight()");
      } else {
        view.getEngine().executeScript(script);
      }
    }
  }

  private static void open(WebView view, String... scripts){
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
