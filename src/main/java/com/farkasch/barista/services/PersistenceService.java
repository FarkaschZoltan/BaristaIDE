package com.farkasch.barista.services;

import com.farkasch.barista.gui.codinginterface.CodingInterface;
import com.farkasch.barista.gui.codinginterface.CodingInterfaceContainer;
import com.farkasch.barista.gui.component.ErrorPopup;
import com.farkasch.barista.gui.component.FolderDropdown.FolderDropdownItem;
import com.farkasch.barista.gui.mainview.sidemenu.SideMenu;
import com.farkasch.barista.util.BaristaProject;
import com.farkasch.barista.util.Result;
import java.io.File;
import java.io.FileNotFoundException;
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
  private SideMenu sideMenu;
  @Lazy
  @Autowired
  private CodingInterfaceContainer codingInterfaceContainer;
  @Lazy
  @Autowired
  private ErrorPopup errorPopup;

  private File activeFile;
  private List<File> openFiles;
  private List<File> mainFiles;
  private List<File> recentlyClosed;
  private CodingInterface activeInterface;
  private BaristaProject openProject;

  public PersistenceService() {
    openFiles = new ArrayList<>();
    recentlyClosed = new ArrayList<>();
    mainFiles = new ArrayList<>();
  }

  public File getActiveFile() {
    return activeFile;
  }

  public void setActiveFile(File activeFile) {
    this.activeFile = activeFile;
  }

  public CodingInterface getActiveInterface() {
    return activeInterface;
  }

  public void setActiveInterface(CodingInterface activeInterface) {
    this.activeInterface = activeInterface;
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

  public SideMenu getSideMenu() {
    return sideMenu;
  }

  public void setSideMenu(SideMenu sideMenu) {
    this.sideMenu = sideMenu;
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
          fileService.createNewInJarJson(f.getAbsolutePath(), null);
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

  public void refreshSideMenu() {
    sideMenu.refresh();
  }

  public void addToProjectDropdown(FolderDropdownItem folder, File file){
    sideMenu.getProjectFolderDropdown().addFolderDropdownItem(folder, file);
  }

  public void openNewFile(File file) {
    codingInterfaceContainer.openFile(file);
  }
}
