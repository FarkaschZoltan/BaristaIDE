package com.farkasch.barista;

import com.farkasch.barista.gui.codinginterface.CodingInterfaceContainer;
import com.farkasch.barista.gui.mainview.topmenu.TopMenu;
import com.farkasch.barista.gui.mainview.sidemenu.SideMenu;
import java.nio.file.Paths;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;


public class MainApp extends Application {
    public static void main(String[] args){
        launch(args);
    }

    private SideMenu sideMenu;
    private CodingInterfaceContainer codeArea;
    private TopMenu topMenu;

    public SideMenu getSideMenu() {
        return sideMenu;
    }

    public void setSideMenu(TextArea sideMenuDummy) {
        this.sideMenu = sideMenu;
    }

    public CodingInterfaceContainer getCodeArea() {
        return codeArea;
    }

    public void setCodeArea(CodingInterfaceContainer codeArea) {
        this.codeArea = codeArea;
    }

    public TopMenu getTopMenu() {
        return topMenu;
    }

    public void setTopMenu(TopMenu topMenu) {
        this.topMenu = topMenu;
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("BaristaIDE");

        sideMenu = new SideMenu(this);
        codeArea = new CodingInterfaceContainer();
        topMenu = new TopMenu(this);

        BorderPane layout = new BorderPane();
        layout.setCenter(codeArea);
        layout.setTop(topMenu);
        layout.setLeft(sideMenu);

        Scene scene = new Scene(layout, 700, 600);
        scene.getStylesheets().add(
            Paths.get("src/main/java/com/farkasch/barista/style.css").toAbsolutePath().toUri()
                .toString());

        sideMenu.setPrefWidth(scene.getWidth() * 0.2);

        stage.setScene(scene);

        stage.show();
    }
}
