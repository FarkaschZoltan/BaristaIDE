package com.farkasch.barista.util.settings;

import java.util.List;

public class JarSetting extends AbstractSetting {

  private String file;
  private List<String> jars;

  public JarSetting(String file, List<String> jars) {
    this.file = file;
    this.jars = jars;
  }

  public String getFile() {
    return file;
  }

  public void setFile(String file) {
    this.file = file;
  }

  public List<String> getJars() {
    return jars;
  }

  public void setJars(List<String> jars) {
    this.jars = jars;
  }
}
