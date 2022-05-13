package com.farkasch.barista.gui.codinginterface;

import com.farkasch.barista.gui.component.ErrorPopup;
import com.farkasch.barista.services.FileService;
import com.farkasch.barista.services.JavaScriptService;
import com.farkasch.barista.services.PersistenceService;
import java.io.File;
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
  private PersistenceService persistenceService;
  @Autowired
  private FileService fileService;
  @Autowired
  private ApplicationContext applicationContext;
  @Autowired
  private ErrorPopup errorPopup;

  private CodingInterfaceContainer parent;
  private SwitchMenu switchMenu;
  private boolean interfaceLoaded = false;
  private WebView content;

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
    switchMenu = applicationContext.getBean(SwitchMenu.class);
    switchMenu.setParent(this);
    content.getEngine()
      .load(this.getClass().getResource("/codinginterface/codearea.html")
        .toExternalForm());

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

    persistenceService.updateShownFiles();
    parent.setActiveInterface(this);
  }

  public void close() {
    parent.closeInterface(this);
  }
}
