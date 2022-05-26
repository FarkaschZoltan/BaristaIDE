package tests.persistenceservice;

import com.farkasch.barista.JavaFxApp;
import com.farkasch.barista.services.PersistenceService;
import com.farkasch.barista.util.BaristaProject;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.testfx.framework.junit.ApplicationTest;

@ActiveProfiles("test")
public class PersistenceServiceTest extends ApplicationTest {
  private PersistenceService persistenceService;
  private ConfigurableApplicationContext applicationContext;

  private File file1;
  private File file2;

  @Override
  public void start(Stage stage) {
    try {
      applicationContext = SpringApplication.run(JavaFxApp.class);
      persistenceService = applicationContext.getBean(PersistenceService.class);
      file1 = new File("src\\test\\resources\\PersistenceServiceTest\\file1.txt").getAbsoluteFile();
      file2 = new File("src\\test\\resources\\PersistenceServiceTest\\file2.txt").getAbsoluteFile();
      file1.createNewFile();
      file2.createNewFile();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Before
  public void init() {
  }

  @After
  public void cleanup() {
    file1.delete();
    file2.delete();
  }

  @Test
  public void activeFileTest() {
    persistenceService.setActiveFile(file1);
    Assert.assertEquals(file1, persistenceService.getActiveFile());
    Assert.assertNotEquals(file2, persistenceService.getActiveFile());
  }

  @Test
  public void fileToOpenTest() {
    persistenceService.setFileToOpen(file1);
    Assert.assertEquals(file1, persistenceService.getFileToOpen());
    Assert.assertNotEquals(file2, persistenceService.getFileToOpen());

    try {
      FileWriter fileWriter = new FileWriter(file1);
      fileWriter.write("This is a test-string for fileToOpenTest!");
      fileWriter.close();

      Assert.assertEquals("This is a test-string for fileToOpenTest!\n", persistenceService.getContentToOpen());
    } catch (IOException e) {
      Assert.fail();
    }
  }

  @Test
  public void fileToSwitchTest() {
    persistenceService.setFileToSwitch(file1);
    Assert.assertEquals(file1, persistenceService.getFileToSwitch());
    Assert.assertNotEquals(file2, persistenceService.getFileToSwitch());

    try {
      FileWriter fileWriter = new FileWriter(file1);
      fileWriter.write("This is a test-string for fileToSwitchTest!");
      fileWriter.close();

      Assert.assertEquals("This is a test-string for fileToSwitchTest!\n", persistenceService.getContentToSwitch());
    } catch (IOException e) {
      Assert.fail();
    }
  }

  @Test
  public void openFilesTest() {
    List<File> openFiles = new ArrayList<>();
    openFiles.add(file1);
    persistenceService.addOpenFile(file1);
    Assert.assertArrayEquals(openFiles.toArray(), persistenceService.getOpenFiles().toArray());

    openFiles.add(file2);
    persistenceService.addOpenFile(file2);
    Assert.assertArrayEquals(openFiles.toArray(), persistenceService.getOpenFiles().toArray());


    openFiles.remove(file1);
    persistenceService.removeOpenFile(file1);
    Assert.assertArrayEquals(openFiles.toArray(), persistenceService.getOpenFiles().toArray());

  }

  @Test
  public void mainFilesTest() {
    List<File> mainFiles = new ArrayList<>();
    mainFiles.add(file1);
    persistenceService.setMainFiles(mainFiles);
    Assert.assertArrayEquals(mainFiles.toArray(), persistenceService.getMainFiles().toArray());

    mainFiles.add(file2);
    persistenceService.addMainFile(file2);
    Assert.assertArrayEquals(mainFiles.toArray(), persistenceService.getMainFiles().toArray());

    mainFiles.remove(file1);
    persistenceService.removeMainFile(file1);
    Assert.assertArrayEquals(mainFiles.toArray(), persistenceService.getMainFiles().toArray());
  }

  @Test
  public void recentlyClosedTest() {
    List<File> recentlyClosed = new ArrayList<>();
    recentlyClosed.add(file1);
    persistenceService.setRecentlyClosed(recentlyClosed);
    Assert.assertArrayEquals(recentlyClosed.toArray(), persistenceService.getRecentlyClosed().toArray());

    recentlyClosed.add(file2);
    persistenceService.addRecentlyClosed(file2);
    Assert.assertArrayEquals(recentlyClosed.toArray(), persistenceService.getRecentlyClosed().toArray());


    recentlyClosed.remove(file1);
    persistenceService.removeRecentlyClosed(file1);
    Assert.assertArrayEquals(recentlyClosed.toArray(), persistenceService.getRecentlyClosed().toArray());

  }

  @Test
  public void openProjectTest() {
    BaristaProject openProject = new BaristaProject();
    openProject.setProjectName("testProject");

    persistenceService.setOpenProject(openProject);
    Assert.assertEquals(openProject, persistenceService.getOpenProject());
  }

  @Test
  public void updateAndGetCurrentMainFilesTest() {
    try {
      FileWriter fileWriter = new FileWriter(file1);
      fileWriter.write("public static void main(String[] args)");
      fileWriter.close();

      fileWriter = new FileWriter(file2);
      fileWriter.write("This is NOT a main file");
      fileWriter.close();

      persistenceService.addOpenFile(file1);
      persistenceService.addOpenFile(file2);
      Assert.assertEquals(file1, persistenceService.updateAndGetCurrentMainFiles().get(0));

      persistenceService.removeOpenFile(file1);
      Assert.assertEquals(0, persistenceService.updateAndGetCurrentMainFiles().size());

      fileWriter = new FileWriter(file1);
      fileWriter.write("                  public           static           void            main(String[         ]         args)              ");
      fileWriter.close();
      persistenceService.addOpenFile(file1);
      Assert.assertEquals(file1, persistenceService.updateAndGetCurrentMainFiles().get(0));
      persistenceService.removeOpenFile(file1);

      fileWriter = new FileWriter(file1);
      fileWriter.write("public class Test{\n"
        + "  public static void main(String[] args) {\n"
        + "    System.out.println(\"Hello World!\");\n"
        + "  }\n"
        + "}");
      fileWriter.close();
      persistenceService.addOpenFile(file1);
      Assert.assertEquals(file1, persistenceService.updateAndGetCurrentMainFiles().get(0));
      persistenceService.removeOpenFile(file1);

    } catch (IOException e) {
      Assert.fail();
    }
  }

}
