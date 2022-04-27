package com.farkasch.barista.util;

import org.springframework.stereotype.Component;

@Component
public class FileTemplates {

  public String mainTemplate(){
    String content =
      "public class Main {"
      + "  public static void main(String args[]) {"
      + "    System.out.println(\"Hello World!\");"
      + "  }"
      + "}";
    return content;
  }
}
