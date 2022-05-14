package com.farkasch.barista.gui.mainview.sidemenu;

import com.farkasch.barista.gui.codinginterface.CodingInterface;
import com.farkasch.barista.gui.codinginterface.CodingInterfaceContainer;
import com.farkasch.barista.gui.component.FolderDropdown;
import com.farkasch.barista.gui.component.FolderDropdown.FolderDropdownItem;
import com.farkasch.barista.gui.component.SimpleDropdown;
import com.farkasch.barista.gui.component.WarningPopup;
import com.farkasch.barista.services.FileService;
import com.farkasch.barista.services.PersistenceService;
import com.farkasch.barista.services.ProcessService;
import com.farkasch.barista.util.BaristaDragBoard;
import com.farkasch.barista.util.BaristaProject;
import com.farkasch.barista.util.settings.RunSetting;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import javafx.beans.property.ListProperty;
import javafx.beans.value.ObservableListValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.util.StringConverter;
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
  @Autowired
  private CodingInterfaceContainer codingInterfaceContainer;

  private GridPane topMenu;
  private VBox content;

  private ScrollPane contentScrollPane;
  private Button compileButton;
  private Button runButton;
  private Button openCommandPromptButton;
  private Button editRunSettingsButton;
  private ComboBox<File> mainFileComboBox;
  private ComboBox<RunSetting> runSettingsComboBox;
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
    topMenu = new GridPane();
    initCompileButton();
    initRunButton();
    initCommandPromptButton();
    initConfigEditButton();
    initRunConfigCombobox();
    initMainFileCombobox();

    topMenu.add(compileButton, 0, 0);
    topMenu.add(runButton, 1, 0);
    topMenu.add(openCommandPromptButton, 2, 0);

    if (openedProject == null) {
      topMenu.add(mainFileComboBox, 0, 1, 3, 1);
    } else {
      topMenu.add(runSettingsComboBox, 0, 1, 2, 1);
      topMenu.add(editRunSettingsButton, 2, 1);
    }
  }

  private void initContent() {
    content = new VBox();
    contentScrollPane = new ScrollPane(content);

    contentScrollPane.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
    contentScrollPane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
    contentScrollPane.setFitToWidth(true);
    contentScrollPane.setFitToHeight(true);

    openFiles = new SimpleDropdown("Open Files", persistenceService.getOpenFiles(), persistenceService, codingInterfaceContainer);
    openFiles.setMaxWidth(Double.MAX_VALUE);

    recentlyClosed = new SimpleDropdown("Recently Closed", persistenceService.getRecentlyClosed(), persistenceService, codingInterfaceContainer);
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
    compileButton.setMaxWidth(Double.MAX_VALUE);
    GridPane.setHgrow(compileButton, Priority.ALWAYS);
    GridPane.setFillWidth(compileButton, true);
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
    runButton.setMaxWidth(Double.MAX_VALUE);
    GridPane.setHgrow(runButton, Priority.ALWAYS);
    GridPane.setFillWidth(runButton, true);
  }

  private void initCommandPromptButton() {
    openCommandPromptButton = new Button("");
    openCommandPromptButton.setGraphic(new FontIcon("mdi-window-maximize"));
    openCommandPromptButton.setGraphicTextGap(0);
    openCommandPromptButton.setTextAlignment(TextAlignment.CENTER);
    openCommandPromptButton.setOnAction(event -> {
      if(openedProject == null){
        processService.openCommandPrompt(mainFileComboBox.getValue().getParentFile());
      } else {
        processService.openCommandPrompt(new File(openedProject.getSourceRoot()));
      }
    });
  }

  private void initConfigEditButton() {
    editRunSettingsButton = new Button("");
    editRunSettingsButton.setGraphic(new FontIcon("mdi-settings"));
    editRunSettingsButton.setGraphicTextGap(0);
    editRunSettingsButton.setTextAlignment(TextAlignment.CENTER);
  }

  private void initRunConfigCombobox() {
    runSettingsComboBox = new ComboBox<>();
  }

  private void initMainFileCombobox() {
    mainFileComboBox = new ComboBox<>();
    mainFileComboBox.setMaxWidth(Double.MAX_VALUE);
    mainFileComboBox.setItems(FXCollections.observableList(persistenceService.getMainFiles()));
    GridPane.setHgrow(mainFileComboBox, Priority.ALWAYS);
    GridPane.setFillWidth(mainFileComboBox, true);
    mainFileComboBox.setConverter(new StringConverter<>() {
      @Override
      public String toString(File file) {
        return file == null ? "" : file.getAbsolutePath();
      }

      @Override
      public File fromString(String s) {
        return s.equals("") ? null : new File(s);
      }
    });
  }

  public void refresh() {
    System.out.println("refresh!");
    openFiles.refresh(persistenceService.getOpenFiles());
    recentlyClosed.refresh(persistenceService.getRecentlyClosed());
    mainFileComboBox.setItems(FXCollections.observableList(persistenceService.updateAndGetCurrentMainFiles()));
    persistenceService.getOpenFiles().stream().map(File::getAbsolutePath).forEach(System.out::println);
    persistenceService.updateAndGetCurrentMainFiles().stream().map(File::getAbsolutePath).forEach(System.out::println);
  }

  public void openProject(BaristaProject baristaProject) {
    if (content.getChildren().contains(projectFolderDropdown)) {
      closeProject();
    } else {
      ArrayList<CodingInterface> toClose = new ArrayList<>();
      codingInterfaceContainer.getInterfaces().forEach(codingInterface -> {
        fileService.saveFile(codingInterface.getShownFile(), codingInterface.getTextContent());
        toClose.add(codingInterface);
      });
      toClose.forEach(CodingInterface::close);
    }

    content.getChildren().remove(openFiles);
    content.getChildren().remove(recentlyClosed);

    openedProject = baristaProject;
    projectFolderDropdown = new FolderDropdown(getWidth(), fileService, warningPopup, true, true);
    projectFolderDropdown.setFileLeftClickAction(
      target -> codingInterfaceContainer.openFile(new File(target.getParentPath() + "\\" + target.getText())));

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

    //giving the absolute parent its own context menu
    projectFolderDropdown.setAbsoluteParentContextMenuItems(Arrays.asList(renameProject, newFile, newFolder, closeProject, deleteProject));
    //enabling file/folder dragging
    projectFolderDropdown.setDragBoard(dragBoard);
    projectFolderDropdown.prepare(openedProject.getProjectRoot(), null);

    content.getChildren().add(projectFolderDropdown);
  }

  public void closeProject() {
    content.getChildren().remove(projectFolderDropdown);
    fileService.saveProject();
    ArrayList<CodingInterface> toClose = new ArrayList<>();
    codingInterfaceContainer.getInterfaces().forEach(codingInterface -> {
      fileService.saveFile(codingInterface.getShownFile(), codingInterface.getTextContent());
      toClose.add(codingInterface);
    });
    toClose.forEach(CodingInterface::close);
    openedProject = null;
    persistenceService.setOpenProject(null);
    content.getChildren().addAll(recentlyClosed, openFiles);
  }

  public FolderDropdown getProjectFolderDropdown() {
    return projectFolderDropdown;
  }
}
