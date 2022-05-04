package com.farkasch.barista.services;

import com.farkasch.barista.gui.codinginterface.SwitchMenu;
import com.farkasch.barista.gui.codinginterface.SwitchMenu.SwitchMenuItem;
import com.farkasch.barista.gui.component.ErrorPopup;
import com.farkasch.barista.gui.component.FolderDropdown.FolderDropdownItem;
import com.farkasch.barista.gui.mainview.sidemenu.SideMenu;
import com.farkasch.barista.util.BaristaProject;
import com.farkasch.barista.util.FileTemplates;
import com.farkasch.barista.util.Result;
import com.google.common.io.Files;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.FileAlreadyExistsException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import javafx.scene.Node;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
public class FileService {

  @Lazy
  @Autowired
  private SideMenu sideMenu;
  @Lazy
  @Autowired
  private ErrorPopup errorPopup;
  @Lazy
  @Autowired
  private PersistenceService persistenceService;
  @Lazy
  @Autowired
  private JavaScriptService javaScriptService;
  @Autowired
  private FileTemplates fileTemplates;

  public void saveFile(File file, String content) {
    try {
      FileOutputStream fos = new FileOutputStream(file, false);
      fos.write(content.getBytes());
      fos.close();

    } catch (IOException e) {
      StringWriter stringWriter = new StringWriter();
      PrintWriter printWriter = new PrintWriter(stringWriter);
      e.printStackTrace(printWriter);
      File errorFile = createErrorLog(stringWriter.toString());
      errorPopup.showWindow(Result.ERROR("Error while saving file!", errorFile));

      printWriter.close();
      e.printStackTrace();
    }
  }

  public File createFile(String path) throws FileAlreadyExistsException {
    File newFile = new File(path);
    try {
      newFile.createNewFile();
    } catch (IOException e) {
      StringWriter stringWriter = new StringWriter();
      PrintWriter printWriter = new PrintWriter(stringWriter);
      e.printStackTrace(printWriter);
      File errorFile = createErrorLog(stringWriter.toString());
      errorPopup.showWindow(Result.ERROR("Error while creating file!", errorFile));

      printWriter.close();
      e.printStackTrace();
    }
    return newFile;
  }

  public File createFile(String path, FolderDropdownItem creationFolder) throws FileAlreadyExistsException {
    File newFile = createFile(path);
    persistenceService.addToProjectDropdown(creationFolder, newFile);
    return newFile;
  }

  public File createFolder(String path) {
    File newFolder = new File(path);
    newFolder.mkdir();
    return newFolder;
  }

  public File createFolder(String path, FolderDropdownItem creationFolder) {
    File newFolder = createFolder(path);
    persistenceService.addToProjectDropdown(creationFolder, newFolder);
    return newFolder;
  }

  public boolean deleteFile(File file, boolean partOfProject){
    if(file.isFile()){
      if(!partOfProject) {
        sideMenu.refresh();
      }
      return file.delete();
    } else {
      return false;
    }
  }
  public boolean deleteFolder(File folder, @Nullable FolderDropdownItem folderDropdownItem){
    return false;
  }

  public void cleanupJarJson() {
    //TODO: implement cleaning up json file after it hasn't been used in a while
  }

  public void createNewInJarJson(String fileName, String... jars) {
    try {
      File jarJsonFile = new File(
        System.getProperty("user.home") + "\\AppData\\Roaming\\BaristaIDE\\config\\JarConfig.json");
      Scanner scanner = new Scanner(jarJsonFile);
      JSONParser parser = new JSONParser();
      JSONObject jar = new JSONObject();
      JSONArray array = new JSONArray();
      String jsonString = "";

      while (scanner.hasNextLine()) {
        jsonString = jsonString.concat(scanner.nextLine());
      }
      scanner.close();

      if (jsonString != "") {
        array = ((JSONArray) parser.parse(jsonString));
      }

      for (Object json : array) {
        if (((JSONObject) json).get("fileName").equals(fileName)) {
          return;
        }
      }

      jar.put("fileName", fileName);
      jar.put("jars", jars == null ? new ArrayList<>() : jars);
      jar.put("lastUpdated", "\"" + LocalDateTime.now() + "\"");

      array.add(jar);
      jsonString = JSONArray.toJSONString(array);
      FileWriter writer = new FileWriter(jarJsonFile);
      writer.write(jsonString);
      writer.close();
    } catch (IOException | ParseException e) {
      StringWriter stringWriter = new StringWriter();
      PrintWriter printWriter = new PrintWriter(stringWriter);
      e.printStackTrace(printWriter);
      File errorFile = createErrorLog(stringWriter.toString());
      errorPopup.showWindow(Result.ERROR("Error while modifying jar configurations!", errorFile));

      printWriter.close();
      e.printStackTrace();
    }

  }

  public void updateNameInJarJson(String oldFileName, String newFileName) {
    try {
      File jarJsonFile = new File(
        System.getProperty("user.home") + "\\AppData\\Roaming\\BaristaIDE\\config\\JarConfig.json");
      Scanner scanner = new Scanner(jarJsonFile);
      JSONParser parser = new JSONParser();
      FileWriter writer = new FileWriter(jarJsonFile);
      String jsonString = "";

      while (scanner.hasNextLine()) {
        jsonString = jsonString.concat(scanner.nextLine());
      }
      scanner.close();
      JSONArray array = (JSONArray) parser.parse(jsonString);

      for (int i = 0; i < array.size(); i++) {
        JSONObject jar = (JSONObject) array.get(i);
        if (jar.get("fileName") == oldFileName) {
          jar.put("fileName", newFileName);
          jar.put("lastUpdated", LocalDateTime.now());
          break;
        }
      }

      jsonString = JSONArray.toJSONString(array);
      writer.write(jsonString);
    } catch (IOException | ParseException e) {
      StringWriter stringWriter = new StringWriter();
      PrintWriter printWriter = new PrintWriter(stringWriter);
      e.printStackTrace(printWriter);
      File errorFile = createErrorLog(stringWriter.toString());
      errorPopup.showWindow(Result.ERROR("Error while modifying jar configurations!", errorFile));

      printWriter.close();
      e.printStackTrace();
    }
  }

  public void updateJarsInJarJson(String fileName, List<String> jars) {
    try {
      File jarJsonFile = new File(
        System.getProperty("user.home") + "\\AppData\\Roaming\\BaristaIDE\\config\\JarConfig.json");
      Scanner scanner = new Scanner(jarJsonFile);
      JSONParser parser = new JSONParser();
      JSONArray array;
      String jsonString = "";

      while (scanner.hasNextLine()) {
        jsonString = jsonString.concat(scanner.nextLine());
      }
      scanner.close();
      if (jsonString == "") {
        array = new JSONArray();
      } else {
        array = ((JSONArray) parser.parse(jsonString));
      }

      for (int i = 0; i < array.size(); i++) {
        JSONObject jar = (JSONObject) array.get(i);
        System.out.println(jar.get("fileName"));
        System.out.println(fileName);
        if (jar.get("fileName").equals(fileName)) {
          System.out.println("jars: ");
          jars.stream().forEach(System.out::println);
          jar.put("jars", jars);
          jar.put("lastUpdated", "\"" + LocalDateTime.now() + "\"");
          break;
        }
      }

      jsonString = JSONArray.toJSONString(array);
      FileWriter writer = new FileWriter(jarJsonFile);
      writer.write(jsonString);
      writer.close();
    } catch (IOException | ParseException e) {
      StringWriter stringWriter = new StringWriter();
      PrintWriter printWriter = new PrintWriter(stringWriter);
      e.printStackTrace(printWriter);
      File errorFile = createErrorLog(stringWriter.toString());
      errorPopup.showWindow(Result.ERROR("Error while modifying jar configurations!", errorFile));

      printWriter.close();
      e.printStackTrace();
    }
  }

  public List<String> getJarsForFile(String fileName) {
    try {
      File jarJsonFile = new File(
        System.getProperty("user.home") + "\\AppData\\Roaming\\BaristaIDE\\config\\JarConfig.json");
      Scanner scanner = new Scanner(jarJsonFile);
      JSONParser parser = new JSONParser();
      String jsonString = "";

      while (scanner.hasNextLine()) {
        jsonString = jsonString.concat(scanner.nextLine());
      }
      scanner.close();
      JSONArray array = (JSONArray) parser.parse(jsonString);

      for (Object j : array) {
        if (((JSONObject) j).get("fileName").equals(fileName)) {
          return (ArrayList<String>) ((JSONObject) j).get("jars");
        }
      }

    } catch (IOException | ParseException e) {
      StringWriter stringWriter = new StringWriter();
      PrintWriter printWriter = new PrintWriter(stringWriter);
      e.printStackTrace(printWriter);
      File errorFile = createErrorLog(stringWriter.toString());
      errorPopup.showWindow(Result.ERROR("Error while accessing jar configurations!", errorFile));

      printWriter.close();
      e.printStackTrace();
    }
    return new ArrayList<String>();
  }

  public void createNewProject(BaristaProject baristaProject) {
    try {
      //creating root folder;
      File projectRoot = new File(baristaProject.getProjectRoot());
      projectRoot.mkdir();

      //creating .barista folder
      File baristaFolder = new File(baristaProject.getProjectRoot() + "\\.barista");
      baristaFolder.mkdir();

      //creating ProjectConfig.json inside .barista
      File projectConfig = new File(baristaFolder.getAbsolutePath() + "\\ProjectConfig.json");
      projectConfig.createNewFile();

      //creating src folder
      File srcFolder = new File(baristaProject.getProjectRoot() + "\\src");
      srcFolder.mkdir();

      //creating main folder
      File mainFolder = new File(srcFolder.getAbsolutePath() + "\\main");
      mainFolder.mkdir();

      //creating java folder
      File javaFolder = new File(mainFolder.getAbsolutePath() + "\\java");
      javaFolder.mkdir();

      //creating main file
      File mainFile = new File(javaFolder.getAbsolutePath() + "\\Main.java");
      mainFile.createNewFile();
      FileWriter writer = new FileWriter(mainFile);
      writer.write(fileTemplates.mainTemplate());
      writer.close();

      //creating target folder
      File targetFolder = new File(baristaProject.getProjectRoot() + "\\target");
      targetFolder.mkdir();

      File globalProjectConfig = new File(System.getProperty("user.home")
        + "\\AppData\\Roaming\\BaristaIDE\\config\\ProjectConfig.json");
      Scanner scanner = new Scanner(globalProjectConfig);
      JSONParser parser = new JSONParser();
      JSONObject project = new JSONObject();
      JSONArray array = new JSONArray();
      String jsonString = "";

      while (scanner.hasNextLine()) {
        jsonString = jsonString.concat(scanner.nextLine());
      }
      scanner.close();

      if (jsonString != "") {
        array = ((JSONArray) parser.parse(jsonString));
      }

      for (Object json : array) {
        if (((JSONObject) json).get("projectName").equals(baristaProject.getProjectName())) {
          return; //TODO create cutsom "RESULT" to return
        }
      }

      project.put("projectName", baristaProject.getProjectName());
      project.put("projectRoot", baristaProject.getProjectRoot());
      project.put("sourceRoot", baristaProject.getSourceRoot());
      project.put("maven", baristaProject.isMaven());
      project.put("gradle", baristaProject.isGradle());

      array.add(project);
      jsonString = JSONArray.toJSONString(array);
      if (!globalProjectConfig.canWrite()) {
        globalProjectConfig.setWritable(true);
      }
      writer = new FileWriter(globalProjectConfig);
      writer.write(jsonString);
      writer.close();

      loadProject(baristaProject);

    } catch (IOException | ParseException e) {
      StringWriter stringWriter = new StringWriter();
      PrintWriter printWriter = new PrintWriter(stringWriter);
      e.printStackTrace(printWriter);
      File errorFile = createErrorLog(stringWriter.toString());
      errorPopup.showWindow(Result.ERROR("Error while creating project!", errorFile));

      printWriter.close();
      e.printStackTrace();
    }
  }

  public void loadProject(BaristaProject baristaProject) {
    sideMenu.openProject(baristaProject);
    persistenceService.setOpenProject(baristaProject);
  }

  public List<BaristaProject> getProjects() {
    try {
      File projectConfig = new File(System.getProperty("user.home")
        + "\\AppData\\Roaming\\BaristaIDE\\config\\ProjectConfig.json");
      Scanner scanner = new Scanner(projectConfig);
      JSONParser parser = new JSONParser();
      JSONArray array = new JSONArray();
      List<BaristaProject> projects = new ArrayList<>();
      String jsonString = "";

      while (scanner.hasNextLine()) {
        jsonString = jsonString.concat(scanner.nextLine());
      }
      scanner.close();

      if (jsonString != "") {
        array = ((JSONArray) parser.parse(jsonString));
      }

      for (Object o : array) {
        System.out.println("project found!");
        JSONObject jso = (JSONObject) o;
        projects.add(
          new BaristaProject((String) jso.get("projectName"), (String) jso.get("projectRoot"),
            (boolean) jso.get("maven"),
            (boolean) jso.get("gradle")));
      }

      return projects;

    } catch (IOException | ParseException e) {
      StringWriter stringWriter = new StringWriter();
      PrintWriter printWriter = new PrintWriter(stringWriter);
      e.printStackTrace(printWriter);
      File errorFile = createErrorLog(stringWriter.toString());
      errorPopup.showWindow(Result.ERROR("Error while accessing projects!", errorFile));

      printWriter.close();
      e.printStackTrace();
    }

    return new ArrayList<>();
  }

  public List<File> getDirs(@Nullable String folderPath) {
    File f = new File(folderPath == null ? System.getProperty("user.home") : folderPath);
    if (f.listFiles(file -> !file.isHidden() && file.isDirectory()) != null) {
      return Arrays.stream(f.listFiles(file -> !file.isHidden() && file.isDirectory())).toList();
    }
    return new ArrayList<>();
  }

  public List<File> getDirsAndFiles(@Nullable String folderPath) {
    File f = new File(folderPath == null ? System.getProperty("user.home") : folderPath);
    if (f.listFiles(file -> !file.isHidden()) != null) {
      return Arrays.stream(f.listFiles(file -> !file.isHidden())).toList();
    }
    return new ArrayList<>();
  }

  private File renameFile(File file, String name) {
    File renamedFile = new File(file.getParent() + "\\" + name);
    System.out.println(renamedFile.getAbsolutePath());
    try {
      Files.move(file, renamedFile);
    } catch (IOException e) {
      StringWriter stringWriter = new StringWriter();
      PrintWriter printWriter = new PrintWriter(stringWriter);
      e.printStackTrace(printWriter);
      File errorFile = createErrorLog(stringWriter.toString());
      errorPopup.showWindow(Result.ERROR("Error while renaming file!", errorFile));

      printWriter.close();
      e.printStackTrace();
    }
    if (persistenceService.getActiveInterface() != null) {
      SwitchMenu switchMenu = persistenceService.getActiveInterface().getSwitchMenu();
      for (Node node : switchMenu.getChildren()) {
        SwitchMenuItem switchMenuItem = (SwitchMenuItem) node;
        if (switchMenuItem.getFile().getAbsolutePath().equals(file.getAbsolutePath())) {
          switchMenuItem.setFile(renamedFile);
          switchMenuItem.setText(renamedFile.getName());
        }
      }
    }
    return renamedFile;
  }

  public File renameFile(File file, String name, @Nullable FolderDropdownItem folderDropdownItem) {
    File renamedFile = renameFile(file, name);
    if (folderDropdownItem == null) {
      for (File f : sideMenu.getOpenFiles().getItems()) {
        if (f.getAbsolutePath().equals(file.getAbsolutePath())) {
          f = renamedFile;
          break;
        }
      }
      for (File f : sideMenu.getRecentlyClosed().getItems()) {
        if (f.getAbsolutePath().equals(file.getAbsolutePath())) {
          f = renamedFile;
          break;
        }
      }
      sideMenu.refresh();
    } else {
      folderDropdownItem.setText(renamedFile.getName());
      if (persistenceService.getActiveFile().equals(file)) {
        persistenceService.setActiveFile(renamedFile);
      }
      renameReferences(new File(persistenceService.getOpenProject().getSourceRoot()),
        file.getName().split("\\.")[0], name.split("\\.")[0]);
    }
    return renamedFile;
  }

  private void renameReferences(File file, String oldReference, String newReference) {
    System.out.println(file.getName());
    System.out.println(file.isFile());
    System.out.println(Files.getFileExtension(file.getAbsolutePath()));
    if (file.isFile() && Files.getFileExtension(file.getAbsolutePath()).equals("java")) {
      try {
        Scanner scanner = new Scanner(file);
        StringBuilder sb = new StringBuilder();
        String line;
        scanner.reset();
        while (scanner.hasNextLine()) {
          line = scanner.nextLine();
          sb.append(line.replaceAll("(?<!\\w)" + oldReference + "(?!\\w)", newReference) + "\n");
        }
        scanner.close();
        saveFile(file, sb.toString());
        if (persistenceService.getActiveFile().equals(file)) {
          javaScriptService.setContent(persistenceService.getActiveInterface().getContentWebView(), sb.toString(), false);
        }
      } catch (FileNotFoundException e) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        File errorFile = createErrorLog(stringWriter.toString());
        errorPopup.showWindow(Result.ERROR("Error while renaming file references!", errorFile));

        printWriter.close();
        e.printStackTrace();
      }
    } else if (file.isDirectory() && file.listFiles() != null) {
      for (File f : file.listFiles()) {
        renameReferences(f, oldReference, newReference);
      }
    }
  }

  public File createErrorLog(String content){
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
    String logName = "error_log_" + format.format(new Date(System.currentTimeMillis())) + ".txt";
    File logFile = new File(System.getProperty("user.home") + "\\AppData\\Roaming\\BaristaIDE\\logs\\" + logName);
    try {
      logFile.createNewFile();
      FileOutputStream fos = new FileOutputStream(logFile);
      fos.write(content.getBytes());
      fos.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return logFile;
  }
}
