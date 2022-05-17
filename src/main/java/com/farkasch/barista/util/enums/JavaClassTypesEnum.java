package com.farkasch.barista.util.enums;

public enum JavaClassTypesEnum {
  CLASS("Class"),
  ENUM("Enum"),
  INTERFACE("Interface"),
  ANNOTATION("Annotation"),
  RECORD("Record");

  private String name;
  private JavaClassTypesEnum(String name){
    this.name = name;
  }

  public String getName(){
    return name;
  }
}
