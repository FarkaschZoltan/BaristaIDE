package com.farkasch.barista.gui.codinginterface;

import com.farkasch.barista.gui.mainview.topmenu.LoadProjectWindow;
import com.farkasch.barista.services.FileService;
import com.farkasch.barista.services.JavaScriptService;
import com.farkasch.barista.services.PersistenceService;
import com.google.common.io.Files;
import java.io.File;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CodingInterface extends BorderPane {

  @Autowired
  private JavaScriptService javaScriptService;
  @Autowired
  private FileService fileService;
  @Autowired
  private ApplicationContext applicationContext;
  @Autowired
  private GenerateWindow generateWindow;

  private CodingInterfaceContainer parent;
  private SwitchMenu switchMenu;
  private boolean interfaceLoaded = false;
  private int generateInsertPosition = 0;
  private WebView content;
  private ContextMenu contextMenu;

  public void setParent(CodingInterfaceContainer parent) {
    this.parent = parent;
    prefWidthProperty().bind(parent.widthProperty());
  }

  public SwitchMenu getSwitchMenu() {
    return switchMenu;
  }

  public WebView getContentWebView() {
    return content;
  }

  @PostConstruct
  private void init() {
    content = new WebView();
    contextMenu = new ContextMenu();
    switchMenu = applicationContext.getBean(SwitchMenu.class);
    switchMenu.setParent(this);

    //this is needed to block CodeMirrors ctrl + Z functionality, as it causes many weird bugs
    content.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
      if(keyEvent.isControlDown() && keyEvent.getCode().equals(KeyCode.Z)){
        keyEvent.consume();
      }
    });

    content.getEngine()
      .load(this.getClass().getResource("/codinginterface/codearea.html")
        .toExternalForm());
    content.setContextMenuEnabled(false);
    content.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
      contextMenu.hide();
      if(event.getButton() == MouseButton.SECONDARY && Files.getFileExtension(getShownFile().getAbsolutePath()).equals("java")){
        generateInsertPosition = fileService.getGenerateInsertPosition(getTextContent(), this);
        contextMenu.show(content, event.getScreenX(), event.getScreenY());
      }
    });

    MenuItem generate = new MenuItem("Generate...");
    generate.setOnAction(event -> generateWindow.showWindow(this, generateInsertPosition));

    contextMenu.getItems().add(generate);

    setTop(switchMenu);
    setCenter(content);
  }

  public String getTextContent() {
    return javaScriptService.getContent(content);
  }

  public File getShownFile() {
    return switchMenu.getCurrentlyActive();
  }

  public void showFileWithClick(File file) {
    showFile(file);
    javaScriptService.setContent(content, file, !interfaceLoaded);
    interfaceLoaded = true;
  }

  public void showFileWithDrag(File file){
    showFile(file);
    javaScriptService.switchContent(content, file, !interfaceLoaded);
    interfaceLoaded = true;
  }

  private void showFile(File file){
    if (switchMenu.getCurrentlyActive() != null) {
      fileService.saveFile(switchMenu.getCurrentlyActive(),
        javaScriptService.getContent(content));
    }

    if (!switchMenu.contains(file)) {
      switchMenu.addFile(file);
    } else {
      switchMenu.switchToFile(file);
    }

    parent.setActiveInterface(this);
  }

  public void close() {
    parent.closeInterface(this);
  }
}
