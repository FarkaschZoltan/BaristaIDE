package com.farkasch.barista.util;

public class BaristaProject {
  private String projectName;
  private String projectRoot;
  private String sourceRoot;
  private boolean maven;
  private boolean gradle;

  public BaristaProject() {}

  public BaristaProject(String projectName, String projectRoot, boolean maven, boolean gradle){
    this.projectName = projectName;
    this.projectRoot = projectRoot;
    this.sourceRoot = projectRoot + "\\src\\main\\java";
    this.maven = maven;
    this.gradle = gradle;
  }

  public String getProjectName() {
    return projectName;
  }

  public void setProjectName(String projectName) {
    this.projectName = projectName;
  }

  public String getProjectRoot() {
    return projectRoot;
  }

  public void setProjectRoot(String projectRoot) {
    this.projectRoot = projectRoot;
  }

  public boolean isMaven() {
    return maven;
  }

  public void setMaven(boolean maven) {
    this.maven = maven;
  }

  public boolean isGradle() {
    return gradle;
  }

  public void setGradle(boolean gradle) {
    this.gradle = gradle;
  }

  public String getSourceRoot() {
    return sourceRoot;
  }

  public void setSourceRoot(String sourceRoot) {
    this.sourceRoot = sourceRoot;
  }
}
