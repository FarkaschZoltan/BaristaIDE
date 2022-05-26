package com.farkasch.barista.util;

import java.io.File;
import java.util.ArrayList;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class BaristaProject {
  private String projectName;
  private String projectRoot;
  private String sourceRoot;
  private String targetFolder;
  private File mainFile;
  private ArrayList<String> jars;
  private ArrayList<String> sourceFiles;
  private ArrayList<String> otherFiles;
  private ArrayList<String> folders;
  private boolean maven;
  private boolean gradle;

  public BaristaProject() {
  }

  public BaristaProject(String projectName, String projectRoot) {
    this.projectName = projectName;
    this.projectRoot = projectRoot;
  }

  public BaristaProject(String projectName, String projectRoot, boolean maven, boolean gradle) {
    this.projectName = projectName;
    this.projectRoot = projectRoot;
    this.sourceRoot = projectRoot + "\\src\\main\\java";
    this.targetFolder = projectRoot + "\\target";
    this.jars = new ArrayList<>();
    this.sourceFiles = new ArrayList<>();
    this.otherFiles = new ArrayList<>();
    this.folders = new ArrayList<>();
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

  public String getTargetFolder() {
    return targetFolder;
  }

  public void setTargetFolder(String targetFolder) {
    this.targetFolder = targetFolder;
  }

  public ArrayList<String> getJars() {
    return jars;
  }

  public void setJars(ArrayList<String> jars) {
    this.jars = jars;
  }

  public ArrayList<String> getSourceFiles() {
    return sourceFiles;
  }

  public void setSourceFiles(ArrayList<String> sourceFiles) {
    this.sourceFiles = sourceFiles;
  }

  public void addSourceFile(File sourceFile) {
    sourceFiles.add(sourceFile.getAbsolutePath());
  }

  public void removeSourceFile(File sourceFile) {
   sourceFiles.remove(sourceFile.getAbsolutePath());
  }

  public File getMainFile() {
    return mainFile;
  }

  public void setMainFile(File mainFile) {
    this.mainFile = mainFile;
  }

  public ArrayList<String> getFolders() {
    return folders;
  }

  public void setFolders(ArrayList<String> folders) {
    this.folders = folders;
  }

  public void addFolder(File folder){
    this.folders.add(folder.getAbsolutePath());
  }

  public void removeFolder(File folder){
    this.folders.remove(folder.getAbsolutePath());
  }

  public ArrayList<String> getOtherFiles() {
    return otherFiles;
  }

  public void setOtherFiles(ArrayList<String> otherFiles) {
    this.otherFiles = otherFiles;
  }

  public void addOtherFile(File otherFile){
    otherFiles.add(otherFile.getAbsolutePath());
  }

  public void removeOtherFile(File otherFile){
    otherFiles.remove(otherFile.getAbsolutePath());
  }

  public ArrayList<String> getAllFiles(){
    ArrayList<String> allFiles = new ArrayList<>();
    allFiles.addAll(sourceFiles);
    allFiles.addAll(otherFiles);
    return allFiles;
  }

  public String toJsonString() {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("projectName", projectName);
    jsonObject.put("projectRoot", projectRoot);
    jsonObject.put("sourceRoot", sourceRoot);
    jsonObject.put("mainFile", mainFile == null ? "" : mainFile.getAbsolutePath());
    jsonObject.put("targetFolder", targetFolder);
    jsonObject.put("jars", jars);
    jsonObject.put("sourceFiles", sourceFiles);
    jsonObject.put("otherFiles", otherFiles);
    jsonObject.put("folders", folders);
    jsonObject.put("maven", maven);
    jsonObject.put("gradle", gradle);

    return jsonObject.toJSONString();
  }

  public void fromJsonString(String jsonString) throws ParseException {
    JSONParser parser = new JSONParser();
    JSONObject jsonObject = (JSONObject) parser.parse(jsonString);

    projectName = (String) jsonObject.get("projectName");
    projectRoot = (String) jsonObject.get("projectRoot");
    sourceRoot = (String) jsonObject.get("sourceRoot");
    targetFolder = (String) jsonObject.get("targetFolder");
    jars = (ArrayList<String>) jsonObject.get("jars");
    sourceFiles = (ArrayList<String>) jsonObject.get("sourceFiles");
    otherFiles = (ArrayList<String>) jsonObject.get("otherFiles");
    folders = (ArrayList<String>) jsonObject.get("folders");
    mainFile = new File((String) jsonObject.get("mainFile"));
    maven = (boolean) jsonObject.get("maven");
    gradle = (boolean) jsonObject.get("gradle");

  }

}
