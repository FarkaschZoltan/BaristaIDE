package com.farkasch.barista.util.enums;

public enum FileExtensionEnum {
  JAVA(".java"),
  TXT(".txt"),
  XML(".xml"),
  GRADLE(".gradle"),
  OTHER("other");

  private String name;

  private FileExtensionEnum(String name){
    this.name = name;
  }

  public String getName(){
    return name;
  }
}
