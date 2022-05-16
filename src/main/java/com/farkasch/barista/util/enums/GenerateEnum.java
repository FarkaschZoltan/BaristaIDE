package com.farkasch.barista.util.enums;

public enum GenerateEnum {
  GETTER("Getter"),
  SETTER("Setter"),
  GETTER_AND_SETTER("Getter and Setter"),
  CONSTRUCTOR("Constructor");

  private String name;

  private GenerateEnum(String name){
    this.name = name;
  }

  public String getName(){
    return name;
  }
}
