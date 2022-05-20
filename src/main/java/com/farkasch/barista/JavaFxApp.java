package com.farkasch.barista;

import com.farkasch.barista.gui.mainview.MainStage;
import java.io.File;
import java.io.IOException;
import javafx.application.Application;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@Configurable
@SpringBootApplication
public class JavaFxApp extends Application {

  private ConfigurableApplicationContext applicationContext;
  private MainStage mainStage;

  public static void main(String[] args) {
    launch();
  }

  @Override
  public void start(Stage stage) throws Exception {
    checkVitalFiles();
    applicationContext = SpringApplication.run(JavaFxApp.class);
    mainStage = applicationContext.getBean(MainStage.class);
    mainStage.show();
  }

  //checks if files and folders crucial for running the program are present on the computer. If any of them are missing, they will be created.
  //this is temporary, as it's job will be done via an installer in the future.
  private void checkVitalFiles(){
    try{
      String home = System.getProperty("user.home");
      File baristaFolder = new File(home + "\\AppData\\Roaming\\BaristaIDE");
      if(!baristaFolder.exists()){
        baristaFolder.mkdir();
      }

      File configFolder = new File(home + "\\AppData\\Roaming\\BaristaIDE\\config");
      if(!configFolder.exists()){
        configFolder.mkdir();
      }

      File jarConfig = new File(home + "\\AppData\\Roaming\\BaristaIDE\\config\\JarConfig.json");
      if(!jarConfig.exists()){
        jarConfig.createNewFile();
      }

      File projectConfig = new File(home + "\\AppData\\Roaming\\BaristaIDE\\config\\ProjectConfig.json");
      if(!projectConfig.exists()){
        projectConfig.createNewFile();
      }

      File logsFolder = new File(home  + "\\AppData\\Roaming\\BaristaIDE\\logs");
      if(!logsFolder.exists()){
        logsFolder.mkdir();
      }
    } catch (IOException e){
      e.printStackTrace();
    }
  }
}
