package com.farkasch.barista.services;

import javafx.scene.web.WebView;

public class JavaScriptService {

    public static String getContent(WebView view) {
        String script = "document.getElementById(\"code-area\").value";
        return (String) view.getEngine().executeScript(script);
    }

    public static void setContent(WebView view, String content) {
        if (!content.equals("")) {
            String script =
                "document.getElementById(\"code-area\").value = " + "\"" + content + "\"";
            view.getEngine().executeScript(script);
        }


    }
}
