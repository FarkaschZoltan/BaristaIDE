package com.farkasch.barista.util.settings;

public class RunSetting extends AbstractSetting {

  private String name;
  private String command;

  public RunSetting(String name, String command) {
    this.name = name;
    this.command = command;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCommand() {
    return command;
  }

  public void setCommand(String command) {
    this.command = command;
  }

  @Override
  public boolean equals(Object other) {
    if (other != null && other.getClass().equals(this.getClass())) {
      if(((RunSetting) other).getName().equals(this.getName())){
        return true;
      } else {
        return false;
      }
    }
    return false;
  }
}
