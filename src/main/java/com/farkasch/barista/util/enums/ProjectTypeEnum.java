package com.farkasch.barista.util.enums;

public enum ProjectTypeEnum {
  BASIC("Basic"), //basic java project
  MAVEN("Maven"), //maven project
  GRADLE("Gradle"); //gradle project

  private String name;
  private ProjectTypeEnum(String name){
    this.name = name;
  }
  public String getName(){
    return name;
  }
}
