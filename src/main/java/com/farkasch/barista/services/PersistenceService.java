package com.farkasch.barista.services;

import com.farkasch.barista.gui.component.ErrorPopup;;
import com.farkasch.barista.util.BaristaProject;
import com.farkasch.barista.util.Result;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class PersistenceService {

  @Lazy
  @Autowired
  private FileService fileService;
  @Lazy
  @Autowired
  private ErrorPopup errorPopup; //for showing errors

  private File activeFile; //The file currently being edited
  private File previouslyActiveFile; //The file previously edited
  private File fileToOpen; //The file, who's data will be sent to be displayed, when it is opened via a click
  private File fileToSwitch; //The file, who's data will be sent to be displayed, when it is opened via a drag
  private List<File> openFiles; //File currently open, when not in a project
  private List<File> mainFiles; //List of available main files, when not in a project
  private List<File> recentlyClosed; //List of recently closed files, when not in a project
  private BaristaProject openProject; //The currently open project
  private String generatedContent; //generated content for the javascript to access
  private int generateInsertPosition; //the row in which the generated content has to be inserted

  public PersistenceService() {
    openFiles = new ArrayList<>();
    recentlyClosed = new ArrayList<>();
    mainFiles = new ArrayList<>();
  }

  public int getGenerateInsertPosition() {
    return generateInsertPosition;
  }

  public void setGenerateInsertPosition(int generateInsertPosition) {
    this.generateInsertPosition = generateInsertPosition;
  }

  public String getGeneratedContent() {
    return generatedContent;
  }

  public void setGeneratedContent(String generatedContent) {
    this.generatedContent = generatedContent;
  }

  public File getFileToOpen() {
    return fileToOpen;
  }

  public void setFileToOpen(File fileToOpen) {
    this.fileToOpen = fileToOpen;
  }

  public File getFileToSwitch() {
    return fileToSwitch;
  }

  public void setFileToSwitch(File fileToSwitch) {
    this.fileToSwitch = fileToSwitch;
  }

  public File getPreviouslyActiveFile() {
    return previouslyActiveFile;
  }

  public void setPreviouslyActiveFile(File previouslyActiveFile) {
    this.previouslyActiveFile = previouslyActiveFile;
  }

  public File getActiveFile() {
    return activeFile;
  }

  public void setActiveFile(File activeFile) {
    this.activeFile = activeFile;
  }

  public List<File> getOpenFiles() {
    return openFiles;
  }

  public void setOpenFiles(List<File> openFiles) {
    this.openFiles = openFiles;
  }

  public void addOpenFile(File file) {
    openFiles.add(file);
    updateAndGetCurrentMainFiles();
    openFiles.stream().map(File::getAbsolutePath).forEach(System.out::println);
    mainFiles.stream().map(File::getAbsolutePath).forEach(System.out::println);
  }

  public void removeOpenFile(File file) {
    openFiles.remove(file);
    updateAndGetCurrentMainFiles();
  }

  public List<File> getMainFiles() {
    return mainFiles;
  }

  public void setMainFiles(List<File> mainFiles) {
    this.mainFiles = mainFiles;
  }

  public void addMainFile(File file) {
    mainFiles.add(file);
  }

  public void removeMainFile(File file) {
    mainFiles.remove(file);
  }

  public List<File> getRecentlyClosed() {
    return recentlyClosed;
  }

  public void setRecentlyClosed(List<File> recentlyClosed) {
    this.recentlyClosed = recentlyClosed;
  }

  public BaristaProject getOpenProject() {
    return openProject;
  }

  public void setOpenProject(BaristaProject openProject) {
    this.openProject = openProject;
  }

  public void addRecentlyClosed(File file) {
    if (recentlyClosed.contains(file)) {
      recentlyClosed.remove(file);
    }
    recentlyClosed.add(file);
  }

  public List<File> updateAndGetCurrentMainFiles() {
    String mainString = "publicstaticvoidmain(String[]args)";
    List<File> newMainFiles = new ArrayList<>();
    for (File f : openFiles) {
      try {
        String fileContent = "";
        Scanner scanner = new Scanner(f);
        while (scanner.hasNextLine()) {
          fileContent = fileContent.concat(scanner.nextLine());
        }
        scanner.close();
        if (fileContent.replaceAll("\\s", "").contains(mainString)) {
          newMainFiles.add(f);
          fileService.addNewJarConfig(f.getAbsolutePath(), null);
        }
      } catch (FileNotFoundException e) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        File errorFile = fileService.createErrorLog(stringWriter.toString());
        errorPopup.showWindow(Result.ERROR("Error while updating main files!", errorFile));

        printWriter.close();
        e.printStackTrace();
      }
    }
    setMainFiles(newMainFiles);
    return newMainFiles;
  }

  //This method is called from highlight.js. It sets the content of the coding interface.
  public String getContentToOpen() {
    try {
      StringBuilder sb = new StringBuilder();
      Scanner scanner = new Scanner(fileToOpen);
      while (scanner.hasNextLine()) {
        sb.append(scanner.nextLine());
        sb.append("\n");
      }
      scanner.close();
      return sb.toString();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return "";
  }

  //This method is called from highlight.js. It sets the content of the coding interface. The only difference to the method above is,
  // that it is used, when a file is opened via drag and drop
  public String getContentToSwitch() {
    try {
      StringBuilder sb = new StringBuilder();
      Scanner scanner = new Scanner(fileToSwitch);
      while (scanner.hasNextLine()) {
        sb.append(scanner.nextLine());
        sb.append("\n");
      }
      scanner.close();
      return sb.toString();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return "";
  }
}
