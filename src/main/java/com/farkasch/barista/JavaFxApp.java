package com.farkasch.barista;

import com.farkasch.barista.gui.mainview.MainStage;
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

  @Override
  public void start(Stage stage) throws Exception {
    applicationContext = SpringApplication.run(JavaFxApp.class);
    mainStage = applicationContext.getBean(MainStage.class);
    mainStage.show();
  }

  public static void main(String[] args) {
    launch();
  }
}
