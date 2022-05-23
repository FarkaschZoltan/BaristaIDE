package com.farkasch.barista.util.enums;

public enum FileExtensionEnum {
  JAVA(".java"),
  TXT(".txt"),
  XML(".xml"),
  OTHER("other");

  private String name;

  FileExtensionEnum(String name){
    this.name = name;
  }

  public String getName(){
    return name;
  }
}
