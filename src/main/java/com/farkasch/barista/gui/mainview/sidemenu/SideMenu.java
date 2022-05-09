package com.farkasch.barista.gui.mainview.sidemenu;

import com.farkasch.barista.gui.component.FolderDropdown;
import com.farkasch.barista.gui.component.FolderDropdown.FolderDropdownItem;
import com.farkasch.barista.gui.component.SimpleDropdown;
import com.farkasch.barista.gui.component.WarningPopup;
import com.farkasch.barista.services.FileService;
import com.farkasch.barista.services.PersistenceService;
import com.farkasch.barista.services.ProcessService;
import com.farkasch.barista.util.BaristaDragBoard;
import com.farkasch.barista.util.BaristaProject;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.RunnableFuture;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.input.Dragboard;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javax.annotation.PostConstruct;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SideMenu extends BorderPane {

  @Autowired
  private ProcessService processService;
  @Autowired
  private PersistenceService persistenceService;
  @Autowired
  private FileService fileService;
  @Autowired
  private NewFilePopup newFilePopup;
  @Autowired
  private NewFolderPopup newFolderPopup;
  @Autowired
  private RenameFolderPopup renameFolderPopup;
  @Autowired
  private RenameFilePopup renameFilePopup;
  @Autowired
  private RenameProjectPopup renameProjectPopup;
  @Autowired
  private WarningPopup warningPopup;
  @Autowired
  private BaristaDragBoard dragBoard;

  private HBox topMenu;
  private VBox content;

  private ScrollPane contentScrollPane;
  private Button compileButton;
  private Button runButton;
  private SimpleDropdown openFiles;
  private SimpleDropdown recentlyClosed;
  private FolderDropdown projectFolderDropdown;
  private BaristaProject openedProject;

  public SimpleDropdown getOpenFiles() {
    return openFiles;
  }

  public SimpleDropdown getRecentlyClosed() {
    return recentlyClosed;
  }

  @PostConstruct
  private void init() {

    openedProject = null;

    setId("side-menu");
    initTopMenu();
    initContent();

    setTop(topMenu);
    setCenter(contentScrollPane);
  }

  private void initTopMenu() {
    topMenu = new HBox();
    initCompileButton();
    initRunButton();

    topMenu.getChildren().addAll(compileButton, runButton);
  }

  private void initContent() {
    content = new VBox();
    contentScrollPane = new ScrollPane(content);

    contentScrollPane.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
    contentScrollPane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
    contentScrollPane.setFitToWidth(true);
    contentScrollPane.setFitToHeight(true);

    openFiles = new SimpleDropdown("Open Files", persistenceService.getOpenFiles(), persistenceService);
    openFiles.setMaxWidth(Double.MAX_VALUE);

    recentlyClosed = new SimpleDropdown("Recently Closed", persistenceService.getRecentlyClosed(), persistenceService);
    recentlyClosed.setMaxWidth(Double.MAX_VALUE);

    content.setId("side-menu__content");
    content.setMaxWidth(Double.MAX_VALUE);
    content.getChildren().addAll(recentlyClosed, openFiles);
    content.setOnDragOver(event -> {
      System.out.println("content");
    });
  }

  private void initCompileButton() {
    compileButton = new Button("Compile");
    compileButton.setGraphic(new FontIcon("mdi-wrench"));
    Runnable compileRunnable = () -> {
      if (persistenceService.getOpenProject() == null) {
        File f = persistenceService.getMainFiles().get(0);
        String filePath = f.getParentFile().getPath();
        String fileName = f.getName();
        File runArgs = processService.CompileFile(filePath, fileName);
        runArgs.delete();
      } else {
        File runArgs = processService.CompileProject(persistenceService.getOpenProject());
        runArgs.delete();
      }
    };
    compileButton.setOnAction(click -> {
      Thread compileThread = new Thread(compileRunnable);
      compileThread.start();
    });
  }

  private void initRunButton() {
    runButton = new Button("Run");
    runButton.setGraphic(new FontIcon("mdi-play"));
    Runnable runRunnable = () -> {
      if (persistenceService.getOpenProject() == null) {
        File f = persistenceService.getMainFiles().get(0);
        String filePath = f.getParentFile().getPath();
        String fileName = f.getName();
        processService.RunFile(filePath, fileName);
      } else {
        processService.RunProject(persistenceService.getOpenProject());
      }
    };
    runButton.setOnAction(click -> {
      Thread runThread = new Thread(runRunnable);
      runThread.start();
    });
  }

  public void refresh() {
    openFiles.refresh(persistenceService.getOpenFiles());
    recentlyClosed.refresh(persistenceService.getRecentlyClosed());
  }

  public void openProject(BaristaProject baristaProject) {
    if (content.getChildren().contains(projectFolderDropdown)) {
      closeProject();
    }

    content.getChildren().remove(openFiles);
    content.getChildren().remove(recentlyClosed);

    openedProject = baristaProject;
    projectFolderDropdown = new FolderDropdown(getWidth(), fileService, true, true);
    projectFolderDropdown.setFileLeftClickAction(
      target -> persistenceService.openNewFile(new File(target.getParentPath() + "\\" + target.getText())));

    MenuItem newFile = new MenuItem("Create New File");
    newFile.setOnAction(click -> newFilePopup.showWindow((FolderDropdownItem) ((MenuItem) click.getTarget()).getParentPopup().getOwnerNode()));

    MenuItem newFolder = new MenuItem("Create New Folder");
    newFolder.setOnAction(click -> newFolderPopup.showWindow((FolderDropdownItem) ((MenuItem) click.getTarget()).getParentPopup().getOwnerNode()));

    MenuItem renameFolder = new MenuItem("Rename");
    renameFolder.setOnAction(
      click -> renameFolderPopup.showWindow((FolderDropdownItem) ((MenuItem) click.getTarget()).getParentPopup().getOwnerNode()));

    MenuItem deleteFolder = new MenuItem("Delete");
    deleteFolder.setOnAction(click -> warningPopup.showWindow("Delete Folder", "Are you sure you want to delete this folder and all of its contents?",
      acceptClick -> fileService.deleteFolder((FolderDropdownItem) ((MenuItem) click.getTarget()).getParentPopup().getOwnerNode()), null));

    projectFolderDropdown.setFolderContextMenuItems(Arrays.asList(renameFolder, newFile, newFolder, deleteFolder));

    MenuItem renameFile = new MenuItem("Rename");
    renameFile.setOnAction(click -> renameFilePopup.showWindow((FolderDropdownItem) ((MenuItem) click.getTarget()).getParentPopup().getOwnerNode()));

    MenuItem deleteFile = new MenuItem("Delete");
    deleteFile.setOnAction(click -> {
      FolderDropdownItem folderDropdownItem = (FolderDropdownItem) ((MenuItem) click.getTarget()).getParentPopup().getOwnerNode();
      warningPopup.showWindow("Delete File", "Are you sure you want to delete this file?",
        acceptClick -> {
          fileService.deleteFile(new File(folderDropdownItem.getPath()), true);
          projectFolderDropdown.removeFolderDropdownItem(folderDropdownItem);
        }, null);

    });
    projectFolderDropdown.setFileContextMenuItems(Arrays.asList(renameFile, deleteFile));

    MenuItem renameProject = new MenuItem("Rename Project");
    renameProject.setOnAction(
      click -> renameProjectPopup.showWindow((FolderDropdownItem) ((MenuItem) click.getTarget()).getParentPopup().getOwnerNode()));

    MenuItem closeProject = new MenuItem("Close Project");
    closeProject.setOnAction(click -> closeProject());

    MenuItem deleteProject = new MenuItem("Delete Project");
    deleteProject.setOnAction(click -> warningPopup.showWindow("Delete Project", "Are you sure you want to delete this project?",
      acceptClick -> fileService.deleteProject(persistenceService.getOpenProject())));

    projectFolderDropdown.setAbsoluteParentContextMenuItems(Arrays.asList(renameProject, newFile, newFolder, closeProject, deleteProject));
    //enabling file/folder dragging
    projectFolderDropdown.setDragBoard(dragBoard);

    projectFolderDropdown.prepare(openedProject.getProjectRoot(), null);

    content.getChildren().add(projectFolderDropdown);
  }

  public void closeProject() {
    content.getChildren().remove(projectFolderDropdown);
    openedProject = null;
    if (persistenceService.getActiveInterface() != null) {
      persistenceService.getActiveInterface().close();
    }
    content.getChildren().addAll(recentlyClosed, openFiles);
  }

  public FolderDropdown getProjectFolderDropdown() {
    return projectFolderDropdown;
  }
}
