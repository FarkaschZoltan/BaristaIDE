package com.farkasch.barista.services;

import com.farkasch.barista.gui.codinginterface.SwitchMenu;
import com.farkasch.barista.gui.codinginterface.SwitchMenu.SwitchMenuItem;
import com.farkasch.barista.gui.component.ErrorPopup;
import com.farkasch.barista.gui.component.FolderDropdown;
import com.farkasch.barista.gui.component.FolderDropdown.FolderDropdownItem;
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
import java.util.stream.Collectors;
import javafx.scene.Node;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

@Service
public class FileService {

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
    if (Files.getFileExtension(newFile.getAbsolutePath()).equals("java")) {
      persistenceService.getOpenProject().addSourceFile(newFile);
      saveProject();
    }
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
    persistenceService.getOpenProject().addFolder(newFolder);
    saveProject();
    return newFolder;
  }

  public boolean deleteFile(File file, boolean partOfProject) {
    if (file.isFile()) {
      if (!partOfProject) {
        persistenceService.getSideMenu().refresh();
      } else {
        persistenceService.getOpenProject().removeSourceFile(file);
        saveProject();
      }
      persistenceService.getActiveInterface().getSwitchMenu().closeFile(file);
      return file.delete();
    } else {
      return false;
    }
  }

  //deleting the specified folder and ALL ITS CONTENTS from the project
  public boolean deleteFolder(FolderDropdownItem folderDropdownItem) {
    System.out.println("delete Folder!");
    //removing the folder, and all its children from the project
    File folderToDelete = new File(folderDropdownItem.getPath());
    BaristaProject baristaProject = persistenceService.getOpenProject();
    baristaProject.setFolders(
      new ArrayList<>(baristaProject.getFolders().stream().filter(folder -> !folder.contains(folderToDelete.getAbsolutePath())).toList()));
    baristaProject.setSourceFiles(
      new ArrayList<>(baristaProject.getSourceFiles().stream().filter(file -> !file.contains(folderToDelete.getAbsolutePath())).toList()));

    if (baristaProject.getSourceRoot().contains(folderToDelete.getAbsolutePath())) {
      baristaProject.setSourceRoot("");
    }

    //removing the folder and its contents from project dropdown
    FolderDropdown projectDropdown = persistenceService.getSideMenu().getProjectFolderDropdown();
    projectDropdown.removeFolderDropdownItem(folderDropdownItem);

    //removing all the files from the switch menu
    if(persistenceService.getActiveInterface() != null) {
      SwitchMenu switchMenu = persistenceService.getActiveInterface().getSwitchMenu();
      List<Node> menusToClose = switchMenu.getChildren().stream()
        .filter(switchMenuItem -> ((SwitchMenuItem) switchMenuItem).getFile().getAbsolutePath().contains(folderToDelete.getAbsolutePath())).toList();
      menusToClose.forEach(item -> switchMenu.closeFile(((SwitchMenuItem) item).getFile()));
    }

    saveProject();
    return FileSystemUtils.deleteRecursively(folderToDelete);
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

  public Result createNewProject(BaristaProject baristaProject) {
    try {
      //creating root folder;
      File projectRoot = new File(baristaProject.getProjectRoot());
      projectRoot.mkdir();
      baristaProject.addFolder(projectRoot);

      //creating .barista folder
      File baristaFolder = new File(baristaProject.getProjectRoot() + "\\.barista");
      baristaFolder.mkdir();
      baristaProject.addFolder(baristaFolder);

      //creating src folder
      File srcFolder = new File(baristaProject.getProjectRoot() + "\\src");
      srcFolder.mkdir();
      baristaProject.addFolder(srcFolder);

      //creating main folder
      File mainFolder = new File(srcFolder.getAbsolutePath() + "\\main");
      mainFolder.mkdir();
      baristaProject.addFolder(mainFolder);

      //creating java folder
      File javaFolder = new File(mainFolder.getAbsolutePath() + "\\java");
      javaFolder.mkdir();
      baristaProject.addFolder(javaFolder);

      //creating main file
      File mainFile = new File(javaFolder.getAbsolutePath() + "\\Main.java");
      mainFile.createNewFile();
      FileWriter writer = new FileWriter(mainFile);
      writer.write(fileTemplates.mainTemplate());
      writer.close();
      baristaProject.addSourceFile(mainFile);
      baristaProject.setMainFile(mainFile);

      //creating target folder
      File targetFolder = new File(baristaProject.getProjectRoot() + "\\target");
      targetFolder.mkdir();
      baristaProject.addFolder(targetFolder);

      //creating ProjectConfig.json inside .barista
      File projectConfig = new File(baristaFolder.getAbsolutePath() + "\\ProjectConfig.json");
      projectConfig.createNewFile();
      writer = new FileWriter(projectConfig);
      writer.write(baristaProject.toJsonString());
      writer.close();

      File globalProjectConfig = new File(System.getProperty("user.home") + "\\AppData\\Roaming\\BaristaIDE\\config\\ProjectConfig.json");
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
          return Result.FAIL("A project with this name already exists!");
        }
      }

      project.put("projectName", baristaProject.getProjectName());
      project.put("projectRoot", baristaProject.getProjectRoot());

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

    return Result.OK();
  }

  public void loadProject(BaristaProject baristaProject) {
    try {
      Scanner scanner = new Scanner(new File(baristaProject.getProjectRoot() + "\\.barista\\ProjectConfig.json"));
      String jsonString = "";

      while (scanner.hasNextLine()) {
        jsonString = jsonString.concat(scanner.nextLine());
      }
      scanner.close();
      baristaProject.fromJsonString(jsonString);

      persistenceService.getSideMenu().openProject(baristaProject);
      persistenceService.setOpenProject(baristaProject);
    } catch (FileNotFoundException | ParseException e) {
      StringWriter stringWriter = new StringWriter();
      PrintWriter printWriter = new PrintWriter(stringWriter);
      e.printStackTrace(printWriter);
      File errorFile = createErrorLog(stringWriter.toString());
      errorPopup.showWindow(Result.ERROR("Error while loading project!", errorFile));

      printWriter.close();
      e.printStackTrace();
    }
  }

  public void saveProject() {
    try {
      File projectConfig = new File(persistenceService.getOpenProject().getProjectRoot() + "\\.barista\\ProjectConfig.json");
      FileWriter writer = new FileWriter(projectConfig);
      writer.write(persistenceService.getOpenProject().toJsonString());
      writer.close();
    } catch (IOException e) {
      StringWriter stringWriter = new StringWriter();
      PrintWriter printWriter = new PrintWriter(stringWriter);
      e.printStackTrace(printWriter);
      File errorFile = createErrorLog(stringWriter.toString());
      errorPopup.showWindow(Result.ERROR("Error while saving project!", errorFile));

      printWriter.close();
      e.printStackTrace();
    }
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
        projects.add(new BaristaProject((String) jso.get("projectName"), (String) jso.get("projectRoot")));
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
    File renamedFile = new File(file.getAbsolutePath().replace(file.getName(), name));
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
      for (File f : persistenceService.getSideMenu().getOpenFiles().getItems()) {
        if (f.getAbsolutePath().equals(file.getAbsolutePath())) {
          f = renamedFile;
          break;
        }
      }
      for (File f : persistenceService.getSideMenu().getRecentlyClosed().getItems()) {
        if (f.getAbsolutePath().equals(file.getAbsolutePath())) {
          f = renamedFile;
          break;
        }
      }
      persistenceService.getSideMenu().refresh();
    } else {
      folderDropdownItem.setText(renamedFile.getName());
      if (persistenceService.getActiveFile().equals(file)) {
        persistenceService.setActiveFile(renamedFile);
      }
      if (Files.getFileExtension(file.getAbsolutePath()).equals("java") && (Files.getFileExtension(name).equals("java") || Files.getFileExtension(
          name)
        .equals(""))) {
        renameReferences(new File(persistenceService.getOpenProject().getSourceRoot()),
          file.getName().split("\\.")[0], name.split("\\.")[0]);
        persistenceService.getOpenProject().removeSourceFile(file);
        persistenceService.getOpenProject().addSourceFile(renamedFile);
      } else if (Files.getFileExtension(file.getAbsolutePath()).equals("java")) {
        persistenceService.getOpenProject().removeSourceFile(file);
      }
      saveProject();
    }
    return renamedFile;
  }

  public void renameFolder(File oldFolder, String name, FolderDropdownItem folderDropdownItem) {
    //renaming the actual file
    File newFolder = new File(oldFolder.getAbsolutePath().replace(oldFolder.getName(), name));
    oldFolder.renameTo(newFolder);
    BaristaProject openProject = persistenceService.getOpenProject();

    //renaming the files/folders inside the project
    openProject.setFolders(new ArrayList<>(
      openProject.getFolders().stream().map(folder -> folder.replace(oldFolder.getAbsolutePath(), newFolder.getAbsolutePath()))
        .collect(Collectors.toList())));
    openProject.getFolders().forEach(System.out::println);
    openProject.setSourceFiles(new ArrayList<>(openProject.getSourceFiles().stream()
      .map(sourceFile -> sourceFile.replace(oldFolder.getAbsolutePath(), newFolder.getAbsolutePath())).toList()));

    if (openProject.getTargetFolder().contains(oldFolder.getAbsolutePath())) {
      openProject.setTargetFolder(openProject.getTargetFolder().replace(oldFolder.getAbsolutePath(), newFolder.getAbsolutePath()));
    }
    if (openProject.getSourceRoot().contains(oldFolder.getAbsolutePath())) {
      openProject.setSourceRoot(openProject.getSourceRoot().replace(oldFolder.getAbsolutePath(), newFolder.getAbsolutePath()));
    }
    if (openProject.getProjectRoot().contains(oldFolder.getAbsolutePath())) {
      openProject.setProjectRoot(openProject.getProjectRoot().replace(oldFolder.getAbsolutePath(), newFolder.getAbsolutePath()));
    }

    //renaming files inside SwitchMenus
    //TODO compatible with multiple interfaces
    if (persistenceService.getActiveInterface() != null) {
      persistenceService.getActiveInterface().getSwitchMenu().getChildren().stream()
        .forEach(child -> ((SwitchMenuItem) child).setFile(new File(((SwitchMenuItem) child).getFile().getAbsolutePath().replace(
          oldFolder.getAbsolutePath(), newFolder.getAbsolutePath()))));
    }

    //renaming the files/folders in the dropdown
    persistenceService.getSideMenu().getProjectFolderDropdown().getRootNode().doActionTopToBottom(item -> {
      item.setParentPath(item.getParentPath().replace(oldFolder.getAbsolutePath(), newFolder.getAbsolutePath()));
    });
    folderDropdownItem.setText(name);
    saveProject();
  }

  private void renameReferences(File file, String oldReference, String newReference) {
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

  public File createErrorLog(String content) {
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
