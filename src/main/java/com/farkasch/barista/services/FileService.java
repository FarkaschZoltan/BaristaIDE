package com.farkasch.barista.services;

import com.farkasch.barista.gui.codinginterface.CodingInterface;
import com.farkasch.barista.gui.codinginterface.CodingInterfaceContainer;
import com.farkasch.barista.gui.codinginterface.SwitchMenu;
import com.farkasch.barista.gui.codinginterface.SwitchMenu.SwitchMenuItem;
import com.farkasch.barista.gui.component.ErrorPopup;
import com.farkasch.barista.gui.component.FolderDropdown;
import com.farkasch.barista.gui.component.FolderDropdown.FolderDropdownItem;
import com.farkasch.barista.gui.mainview.sidemenu.SideMenu;
import com.farkasch.barista.util.BaristaProject;
import com.farkasch.barista.util.FileTemplates;
import com.farkasch.barista.util.Result;
import com.farkasch.barista.util.settings.RunSetting;
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
  @Lazy
  @Autowired
  private CodingInterfaceContainer codingInterfaceContainer;
  @Lazy
  @Autowired
  private SideMenu sideMenu;
  @Autowired
  private FileTemplates fileTemplates;

  public void saveFile(File file, String content) {
    try {
      FileOutputStream fos = new FileOutputStream(file, false);
      fos.write(content.getBytes());
      fos.close();

      if (persistenceService.getOpenProject() == null) {
        sideMenu.refresh();
      }

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
    sideMenu.getProjectFolderDropdown().addFolderDropdownItem(creationFolder, newFile);
    if (Files.getFileExtension(newFile.getAbsolutePath()).equals("java")) {
      persistenceService.getOpenProject().addSourceFile(newFile);
      saveProject();
    }
    return newFile;
  }

  public File createFolder(String path) {
    File newFolder = new File(path);
    System.out.println(path);
    System.out.println(newFolder.mkdir());
    return newFolder;
  }

  public File createFolder(String path, FolderDropdownItem creationFolder) {
    File newFolder = createFolder(path);
    sideMenu.getProjectFolderDropdown().addFolderDropdownItem(creationFolder, newFolder);
    persistenceService.getOpenProject().addFolder(newFolder);
    saveProject();
    return newFolder;
  }

  public boolean deleteFile(File file, boolean partOfProject) {
    if (file.isFile()) {
      if (!partOfProject) {
        sideMenu.refresh();
      } else {
        persistenceService.getOpenProject().removeSourceFile(file);
        saveProject();
      }
      for (CodingInterface codingInterface : codingInterfaceContainer.getInterfaces()) {
        codingInterface.getSwitchMenu().closeFile(file);
      }
      return file.delete();
    } else {
      return false;
    }
  }

  //deleting the specified folder and ALL ITS CONTENTS from the project
  public boolean deleteFolder(FolderDropdownItem folderDropdownItem) {

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
    FolderDropdown projectDropdown = sideMenu.getProjectFolderDropdown();
    projectDropdown.removeFolderDropdownItem(folderDropdownItem);

    //removing all the files from the switch menu(s)
    for (CodingInterface codingInterface : codingInterfaceContainer.getInterfaces()) {
      SwitchMenu switchMenu = codingInterface.getSwitchMenu();
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

  public void addNewJarConfig(String fileName, String... jars) {
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

  public void updateNameInJarConfig(String oldFileName, String newFileName) {
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

  public void updateJarsInJarConfig(String fileName, List<String> jars) {
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
    return new ArrayList<>();
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

      //creating RunConfig.json inside .barista
      File runConfig = new File(baristaFolder.getAbsolutePath() + "\\RunConfig.json");
      runConfig.createNewFile();

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

      sideMenu.openProject(baristaProject);
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

  //deletes given project
  public void deleteProject(BaristaProject baristaProject) {
    try {
      File globalProjectConfig = new File(System.getProperty("user.home") + "\\AppData\\Roaming\\BaristaIDE\\config\\ProjectConfig.json");
      Scanner scanner = new Scanner(globalProjectConfig);
      JSONParser parser = new JSONParser();
      JSONArray array = new JSONArray();
      String jsonString = "";

      while (scanner.hasNextLine()) {
        jsonString = jsonString.concat(scanner.nextLine());
      }
      scanner.close();

      if (!jsonString.equals("")) {
        array = ((JSONArray) parser.parse(jsonString));
      }

      JSONObject toDelete = null;
      for (Object json : array) {
        if (((JSONObject) json).get("projectRoot").equals(baristaProject.getProjectRoot())) {
          toDelete = (JSONObject) json;
          break;
        }
      }

      FileSystemUtils.deleteRecursively(new File(baristaProject.getProjectRoot()));
      if (persistenceService.getOpenProject().getProjectRoot().equals(baristaProject.getProjectRoot())) {
        sideMenu.closeProject(true);
      }
      array.remove(toDelete);

      jsonString = JSONArray.toJSONString(array);
      FileWriter writer = new FileWriter(globalProjectConfig);
      writer.write(jsonString);
      writer.close();
    } catch (IOException | ParseException e) {
      StringWriter stringWriter = new StringWriter();
      PrintWriter printWriter = new PrintWriter(stringWriter);
      e.printStackTrace(printWriter);
      File errorFile = createErrorLog(stringWriter.toString());
      errorPopup.showWindow(Result.ERROR("Error while deleting project!", errorFile));

      printWriter.close();
      e.printStackTrace();
    }
  }

  public void renameProject(String name, FolderDropdownItem folderDropdownItem) {
    BaristaProject baristaProject = persistenceService.getOpenProject();
    String oldProjectRoot = baristaProject.getProjectRoot();
    baristaProject.setProjectName(name);
    renameFolder(new File(baristaProject.getProjectRoot()), name, folderDropdownItem);
    saveProject();

    try {
      File globalProjectConfig = new File(System.getProperty("user.home") + "\\AppData\\Roaming\\BaristaIDE\\config\\ProjectConfig.json");
      Scanner scanner = new Scanner(globalProjectConfig);
      JSONParser parser = new JSONParser();
      JSONArray array = new JSONArray();
      String jsonString = "";

      while (scanner.hasNextLine()) {
        jsonString = jsonString.concat(scanner.nextLine());
      }
      scanner.close();

      if (!jsonString.equals("")) {
        array = ((JSONArray) parser.parse(jsonString));
      }

      for (Object json : array) {
        if (((JSONObject) json).get("projectRoot").equals(oldProjectRoot)) {
          ((JSONObject) json).replace("projectName", baristaProject.getProjectName());
          ((JSONObject) json).replace("projectRoot", baristaProject.getProjectRoot());
          break;
        }
      }

      jsonString = JSONArray.toJSONString(array);
      FileWriter writer = new FileWriter(globalProjectConfig);
      writer.write(jsonString);
      writer.close();

    } catch (IOException | ParseException e) {
      StringWriter stringWriter = new StringWriter();
      PrintWriter printWriter = new PrintWriter(stringWriter);
      e.printStackTrace(printWriter);
      File errorFile = createErrorLog(stringWriter.toString());
      errorPopup.showWindow(Result.ERROR("Error while renaming project!", errorFile));

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

      if (!jsonString.equals("")) {
        array = ((JSONArray) parser.parse(jsonString));
      }

      for (Object o : array) {
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

  public List<RunSetting> getRunConfig() {
    File runConfig = new File(persistenceService.getOpenProject().getProjectRoot() + "\\" + ".barista\\RunConfig.json");
    try {
      Scanner scanner = new Scanner(runConfig);
      JSONParser parser = new JSONParser();
      JSONArray array = new JSONArray();
      List<RunSetting> runSettings = new ArrayList<>();
      String jsonString = "";

      while (scanner.hasNextLine()) {
        jsonString = jsonString.concat(scanner.nextLine());
      }
      scanner.close();

      if (!jsonString.equals("")) {
        array = ((JSONArray) parser.parse(jsonString));
      }

      //adding the basic run config
      runSettings.add(new RunSetting(persistenceService.getOpenProject().getProjectName(), null));
      //adding the custom run configs
      for (Object o : array) {
        JSONObject jsonObject = (JSONObject) o;
        runSettings.add(new RunSetting((String)(jsonObject.get("name")), (String)(jsonObject.get("command"))));
      }

      return runSettings;
    } catch (ParseException | FileNotFoundException e) {
      StringWriter stringWriter = new StringWriter();
      PrintWriter printWriter = new PrintWriter(stringWriter);
      e.printStackTrace(printWriter);
      File errorFile = createErrorLog(stringWriter.toString());
      errorPopup.showWindow(Result.ERROR("Error while accessing run configurations!", errorFile));

      printWriter.close();
      e.printStackTrace();
    }

    return new ArrayList<>();
  }

  public void setRunConfig(List<RunSetting> runSettings){
    File runConfig = new File(persistenceService.getOpenProject().getProjectRoot() + "\\" + ".barista\\RunConfig.json");
    try {
      FileWriter fileWriter = new FileWriter(runConfig);
      JSONArray array = new JSONArray();

      for(RunSetting runSetting : runSettings){
        if(runSetting.getCommand() != null){
          JSONObject jsonObject = new JSONObject();
          jsonObject.put("name", runSetting.getName());
          jsonObject.put("command", runSetting.getCommand());
          array.add(jsonObject);
        }
      }

      fileWriter.write(array.toJSONString());
      fileWriter.close();
    } catch (IOException e) {
      StringWriter stringWriter = new StringWriter();
      PrintWriter printWriter = new PrintWriter(stringWriter);
      e.printStackTrace(printWriter);
      File errorFile = createErrorLog(stringWriter.toString());
      errorPopup.showWindow(Result.ERROR("Error while saving run configurations!", errorFile));

      printWriter.close();
      e.printStackTrace();
    }
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
    for (CodingInterface codingInterface : codingInterfaceContainer.getInterfaces()) {
      SwitchMenu switchMenu = codingInterface.getSwitchMenu();
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
      List<File> openFiles = sideMenu.getOpenFiles().getItems();
      List<File> recentlyClosed = sideMenu.getRecentlyClosed().getItems();
      if (openFiles.contains(file)) {
        openFiles.set(openFiles.indexOf(file), renamedFile);
      }
      if (recentlyClosed.contains(file)) {
        recentlyClosed.set(recentlyClosed.indexOf(file), renamedFile);
      }
      sideMenu.refresh();
    } else {
      folderDropdownItem.setText(renamedFile.getName());
      if (persistenceService.getActiveFile() != null && persistenceService.getActiveFile().equals(file)) {
        persistenceService.setActiveFile(renamedFile);
      }
      if (Files.getFileExtension(file.getAbsolutePath()).equals("java") && (Files.getFileExtension(name).equals("java") || Files.getFileExtension(
        name).equals(""))) {
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
    if (openProject.getMainFile().getAbsolutePath().contains(oldFolder.getAbsolutePath())) {
      openProject.setMainFile(
        new File(openProject.getMainFile().getAbsolutePath().replace(oldFolder.getAbsolutePath(), newFolder.getAbsolutePath())));
    }

    //renaming files inside SwitchMenus
    for (CodingInterface codingInterface : codingInterfaceContainer.getInterfaces()) {
      codingInterface.getSwitchMenu().getChildren().stream().forEach(child -> ((SwitchMenuItem) child).setFile(
        new File(((SwitchMenuItem) child).getFile().getAbsolutePath().replace(oldFolder.getAbsolutePath(), newFolder.getAbsolutePath()))));
    }

    //renaming the files/folders in the dropdown
    folderDropdownItem.setText(name);
    sideMenu.getProjectFolderDropdown().getRootNode().doActionTopToBottom(item -> {
      item.setParentPath(item.getParentPath().replace(oldFolder.getAbsolutePath(), newFolder.getAbsolutePath()));
    });
    saveProject();
  }

  public Result moveFile(File fileToMove, String destinationFolder) {
    try {
      File destinationFile = new File(destinationFolder + "\\" + fileToMove.getName());
      if (destinationFile.exists()) {
        return Result.FAIL("A file with the same name already exists here!");
      }
      BaristaProject baristaProject = persistenceService.getOpenProject();
      repackage(fileToMove, destinationFile);
      redoImports(fileToMove, destinationFile);

      if (fileToMove.getAbsolutePath().equals(baristaProject.getMainFile().getAbsolutePath())) {
        baristaProject.setMainFile(destinationFile);
      }
      if (persistenceService.getActiveFile() != null && persistenceService.getActiveFile().equals(fileToMove)) {
        persistenceService.setActiveFile(destinationFile);
      }
      //changing file path in project config
      baristaProject.removeSourceFile(fileToMove);
      baristaProject.addSourceFile(destinationFile);
      //changing the file in the switch menu(s)
      for (CodingInterface codingInterface : codingInterfaceContainer.getInterfaces()) {
        for (Node node : codingInterface.getSwitchMenu().getChildren()) {
          SwitchMenuItem item = (SwitchMenuItem) node;
          if (item.getFile().getAbsolutePath().equals(fileToMove.getAbsolutePath())) {
            item.setFile(destinationFile);
          }
        }
      }
      //moving the actual file
      saveProject();
      Files.move(fileToMove, destinationFile);
      return Result.OK(destinationFile);
    } catch (IOException e) {
      StringWriter stringWriter = new StringWriter();
      PrintWriter printWriter = new PrintWriter(stringWriter);
      e.printStackTrace(printWriter);
      File errorFile = createErrorLog(stringWriter.toString());
      errorPopup.showWindow(Result.ERROR("Error while moving file!", errorFile));

      printWriter.close();
      e.printStackTrace();
    }
    return Result.FAIL();
  }

  public Result moveFolder(File directoryToMove, File targetDirectory) {
    if (new File(targetDirectory.getAbsolutePath() + "\\" + directoryToMove.getName()).exists()) {
      return Result.FAIL("A folder with this name already exists here!");
    }
    try {
      //moving the actual directory
      recursiveMove(directoryToMove, targetDirectory);
      FileSystemUtils.deleteRecursively(directoryToMove);
      saveProject();
      return Result.OK(new File(targetDirectory.getAbsolutePath() + "\\" + directoryToMove.getName()));
    } catch (IOException e) {
      StringWriter stringWriter = new StringWriter();
      PrintWriter printWriter = new PrintWriter(stringWriter);
      e.printStackTrace(printWriter);
      File errorFile = createErrorLog(stringWriter.toString());
      errorPopup.showWindow(Result.ERROR("Error while reformatting imports!", errorFile));

      printWriter.close();
      e.printStackTrace();
    }
    return Result.FAIL();
  }

  private void recursiveMove(File item, File targetDirectory) throws IOException {
    BaristaProject baristaProject = persistenceService.getOpenProject();
    File newItem = new File(targetDirectory.getAbsolutePath() + "\\" + item.getName());
    if (item.isFile()) {
      moveFile(item, targetDirectory.getAbsolutePath());
    } else {
      //editing folders in the project
      for (String folderPath : baristaProject.getSourceFiles()) {
        if (folderPath.equals(item.getAbsolutePath())) {
          baristaProject.getFolders().remove(folderPath);
          baristaProject.getFolders().add(newItem.getAbsolutePath());
          break;
        }
      }
      //if the folder changed is "special" we change that in the project as well
      if (baristaProject.getTargetFolder().equals(item.getAbsolutePath())) {
        baristaProject.setTargetFolder(newItem.getAbsolutePath());
      }
      if (baristaProject.getProjectRoot().equals(item.getAbsolutePath())) {
        baristaProject.setProjectRoot(newItem.getAbsolutePath());
      }
      if (baristaProject.getSourceRoot().equals(item.getAbsolutePath())) {
        baristaProject.setSourceRoot(newItem.getAbsolutePath());
      }
      newItem.mkdir();
      for (File file : item.listFiles()) {
        recursiveMove(file, newItem);
      }
    }
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
        for (CodingInterface codingInterface : codingInterfaceContainer.getInterfaces()) {
          if (codingInterface.getShownFile().equals(file)) {
            javaScriptService.setContent(codingInterface.getContentWebView(), file, false);
            break;
          }
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

  private void repackage(File fileToMove, File destinationFile) {
    try {
      String packageString = fileTemplates.createPackage(destinationFile.getParent()).trim();
      Scanner scanner = new Scanner(fileToMove);
      StringBuilder sb = new StringBuilder();
      boolean hasPackage = false;
      boolean firstLine = true;
      boolean isLeading = true;
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        if (!(line.equals("") && isLeading)) {
          isLeading = false;
          if (firstLine) {
            String newLine = line.replaceAll("\\s*(package\\s.*;)", packageString);
            if (!newLine.equals(line)) {
              line = newLine;
              hasPackage = true;
            }
            firstLine = false;
          }
          sb.append(line);
          sb.append("\n");
        }
      }
      scanner.close();

      if (!hasPackage) {
        sb.insert(0, packageString + "\n\n");
      }

      FileOutputStream fos = new FileOutputStream(fileToMove);
      fos.write(sb.toString().getBytes());
      fos.close();

      for (CodingInterface codingInterface : codingInterfaceContainer.getInterfaces()) {
        if (codingInterface.getShownFile().equals(fileToMove)) {
          javaScriptService.setContent(codingInterface.getContentWebView(), fileToMove, false);
          break;
        }
      }

    } catch (IOException e) {
      StringWriter stringWriter = new StringWriter();
      PrintWriter printWriter = new PrintWriter(stringWriter);
      e.printStackTrace(printWriter);
      File errorFile = createErrorLog(stringWriter.toString());
      errorPopup.showWindow(Result.ERROR("Error while repackaging file!", errorFile));

      printWriter.close();
      e.printStackTrace();
    }
  }

  private void redoImports(File fileToMove, File destinationFile) {
    try {
      String oldImportString = fileTemplates.createImport(fileToMove.getAbsolutePath()).trim();
      String newImportString = fileTemplates.createImport(destinationFile.getAbsolutePath()).trim();
      System.out.println(oldImportString);
      System.out.println(newImportString);

      for (String path : persistenceService.getOpenProject().getSourceFiles()) {
        boolean endOfImports = false;
        Scanner scanner = new Scanner(new File(path));
        StringBuilder sb = new StringBuilder();
        while (scanner.hasNextLine()) {
          String line = scanner.nextLine();
          if (!endOfImports) {
            if (!(line.contains("package") || line.contains("import") || line.equals(""))) {
              endOfImports = true;
            } else {
              System.out.println(line);
              line = line.replace(oldImportString, newImportString);
              System.out.println(line);
            }
          }
          sb.append(line);
          sb.append("\n");
        }
        scanner.close();
        FileOutputStream fos = new FileOutputStream(path);
        fos.write(sb.toString().trim().getBytes());
        fos.close();

        for (CodingInterface codingInterface : codingInterfaceContainer.getInterfaces()) {
          if (codingInterface.getShownFile().equals(new File(path))) {
            javaScriptService.setContent(codingInterface.getContentWebView(), new File(path), false);
            break;
          }
        }
      }
    } catch (IOException e) {
      StringWriter stringWriter = new StringWriter();
      PrintWriter printWriter = new PrintWriter(stringWriter);
      e.printStackTrace(printWriter);
      File errorFile = createErrorLog(stringWriter.toString());
      errorPopup.showWindow(Result.ERROR("Error while reformatting imports!", errorFile));

      printWriter.close();
      e.printStackTrace();
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

  public boolean folderContains(String folderPath, String itemPath) {
    String pathDiff = itemPath.replace(folderPath + "\\", "");
    if (!pathDiff.equals(itemPath) && pathDiff.length() > 0) {
      return true;
    }
    return false;
  }
}
