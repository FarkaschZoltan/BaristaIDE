package com.farkasch.barista.util.settings;

import com.farkasch.barista.util.enums.JavacEnum;
import java.io.File;
import java.util.HashMap;

public class RunSetting extends AbstractSetting {

  private String name;
  private File mainFile;
  private HashMap<JavacEnum, Object> args;

  public RunSetting(String name, File mainFile, HashMap<JavacEnum, Object> args) {

    this.name = name;
    this.mainFile = mainFile;
    this.args = args;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public File getMainFile() {
    return mainFile;
  }

  public void setMainFile(File mainFile) {
    this.mainFile = mainFile;
  }

  public HashMap<JavacEnum, Object> getArgs() {
    return args;
  }

  public void setArgs(HashMap<JavacEnum, Object> args) {
    this.args = args;
  }
}
