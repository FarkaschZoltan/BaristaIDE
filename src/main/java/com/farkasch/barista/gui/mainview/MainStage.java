package com.farkasch.barista.gui.mainview;

import com.farkasch.barista.gui.codinginterface.CodingInterfaceContainer;
import com.farkasch.barista.gui.mainview.sidemenu.SideMenu;
import com.farkasch.barista.gui.mainview.topmenu.TopMenu;
import java.io.File;
import java.nio.file.Paths;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MainStage extends Stage {

  @Autowired
  private SideMenu sideMenu;
  @Autowired
  private CodingInterfaceContainer codingInterfaceContainer;
  @Autowired
  private TopMenu topMenu;

  private BorderPane layout;
  private Scene scene;

  @PostConstruct
  private void init(){
    setTitle("BaristaIDE");

    layout = new BorderPane();
    scene = new Scene(layout, 800, 600);

    layout.setCenter(codingInterfaceContainer);
    layout.setTop(topMenu);
    layout.setLeft(sideMenu);

    scene.getStylesheets().add("style.css");

    sideMenu.setPrefWidth(scene.getWidth() * 0.1);
    sideMenu.setMinWidth(200);

    setMaximized(true);
    setScene(scene);
  }

}
