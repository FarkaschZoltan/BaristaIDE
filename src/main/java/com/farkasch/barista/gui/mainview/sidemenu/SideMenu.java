package com.farkasch.barista.gui.mainview.sidemenu;

import com.farkasch.barista.gui.component.FolderDropdown;
import com.farkasch.barista.gui.component.SimpleDropdown;
import com.farkasch.barista.services.PersistenceService;
import com.farkasch.barista.services.ProcessService;
import com.farkasch.barista.util.BaristaProject;
import java.io.File;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
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

  private HBox topMenu;
  private VBox content;

  private ScrollPane contentScrollPane;
  private Button compileButton;
  private Button runButton;
  private SimpleDropdown openFiles;
  private SimpleDropdown recentlyClosed;
  private FolderDropdown projectFolderDropdown;
  private BaristaProject openedProject;

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
  }

  private void initCompileButton() {
    compileButton = new Button("Compile");
    compileButton.setGraphic(new FontIcon("mdi-wrench"));
    compileButton.setOnAction(click -> {
      File f = persistenceService.getMainFiles().get(0);
      String filePath = f.getParentFile().getPath();
      String fileName = f.getName();
      //TODO: main file selector
      processService.Compile(filePath, fileName);
    });
  }

  private void initRunButton() {
    runButton = new Button("Run");
    runButton.setGraphic(new FontIcon("mdi-play"));
    runButton.setOnAction(click -> {
      File f = persistenceService.getMainFiles().get(0);
      String filePath = f.getParentFile().getPath();
      String fileName = f.getName();
      processService.Run(filePath, fileName);
    });
  }
  public void refresh(){
    openFiles.refresh(persistenceService.getOpenFiles());
    recentlyClosed.refresh(persistenceService.getRecentlyClosed());
  }

  public void openProject(BaristaProject baristaProject){
    if(content.getChildren().contains(projectFolderDropdown)){
      closeProject();
    }

    openedProject = baristaProject;
    projectFolderDropdown = new FolderDropdown(getWidth(), processService, true, true);
    projectFolderDropdown.prepare(openedProject.getProjectRoot(), null);

    content.getChildren().add(projectFolderDropdown);
  }

  public void closeProject(){
    content.getChildren().remove(projectFolderDropdown);
    openedProject = null;
  }

}
