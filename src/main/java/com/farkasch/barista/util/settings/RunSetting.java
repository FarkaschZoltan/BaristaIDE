package com.farkasch.barista.util.settings;

import com.farkasch.barista.util.enums.JavacEnum;
import java.io.File;
import java.util.HashMap;

public class RunSetting extends AbstractSetting {

  private String name;
  private String command;

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
}
