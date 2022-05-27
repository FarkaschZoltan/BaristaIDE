package tests.fileservice;

import com.farkasch.barista.JavaFxApp;
import com.farkasch.barista.gui.codinginterface.CodingInterfaceContainer;
import com.farkasch.barista.gui.component.ErrorPopup;
import com.farkasch.barista.gui.mainview.sidemenu.SideMenu;
import com.farkasch.barista.services.FileService;
import com.farkasch.barista.services.JavaScriptService;
import com.farkasch.barista.services.PersistenceService;
import com.farkasch.barista.util.FileTemplates;
import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javafx.stage.Stage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.testfx.framework.junit.ApplicationTest;
import tests.testhelper.TestHelper;

@ActiveProfiles("test")
public class JarConfigOperationsTest extends ApplicationTest {

  private FileService fileService;
  private PersistenceService persistenceService;
  private ApplicationContext applicationContext;

  private SideMenu sideMenu;
  private ErrorPopup errorPopup;
  private JavaScriptService javaScriptService;
  private CodingInterfaceContainer codingInterfaceContainer;
  private FileTemplates fileTemplates;

  private HashMap<String, Object> beansToReplace;
  private File jarConfig;
  private String jarConfigContent;
  private JSONArray jarConfigArray;

  @Override
  public void start(Stage stage) {
    try {
      applicationContext = SpringApplication.run(JavaFxApp.class);
      persistenceService = applicationContext.getBean(PersistenceService.class);
      fileService = applicationContext.getBean(FileService.class);

      beansToReplace = new HashMap<>();
      jarConfig = new File(System.getProperty("user.home") + "\\AppData\\Roaming\\BaristaIDE\\config\\JarConfig.json");
      jarConfigContent = TestHelper.copyFileContent(jarConfig);
      jarConfigArray = TestHelper.readIntoJsonArray(jarConfig);

      //mocking all classes inside FileService
      sideMenu = Mockito.mock(SideMenu.class);
      errorPopup = Mockito.mock(ErrorPopup.class);
      javaScriptService = Mockito.mock(JavaScriptService.class);
      codingInterfaceContainer = Mockito.mock(CodingInterfaceContainer.class);
      fileTemplates = Mockito.mock(FileTemplates.class);

      beansToReplace.put("sideMenu", sideMenu);
      beansToReplace.put("errorPopup", errorPopup);
      beansToReplace.put("javascriptService", javaScriptService);
      beansToReplace.put("codingInterfaceContainer", codingInterfaceContainer);
      beansToReplace.put("fileTemplates", fileTemplates);

      fileService.prepareForTesting(beansToReplace);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  @After
  public void cleanup(){
    TestHelper.insertFileContent(jarConfig, jarConfigContent);
  }

  @Test
  public void addNewJarConfigTest() {
    String testFile1 = TestHelper.generateRandomString();
    String testFile2 = TestHelper.generateRandomString();
    String testJar1 = TestHelper.generateRandomString();
    String testJar2 = TestHelper.generateRandomString();

    fileService.addNewJarConfig(testFile1, null);
    Assert.assertTrue(TestHelper.copyFileContent(jarConfig).contains("\"fileName\":\"" + testFile1 + "\",\"jars\":[]"));

    fileService.addNewJarConfig(testFile2, List.of(testJar1, testJar2));
    Assert.assertTrue(
      TestHelper.copyFileContent(jarConfig).contains("\"fileName\":\"" + testFile2 + "\",\"jars\":[\"" + testJar1 + "\",\"" + testJar2 + "\"]"));
  }

  @Test
  public void updateNameInJarConfigTest() {
    String fileToRename = TestHelper.generateRandomString();
    String newName = TestHelper.generateRandomString();

    JSONObject jsonObject = new JSONObject();
    jsonObject.put("fileName", fileToRename);
    jsonObject.put("lastUpdated", "\"" + LocalDateTime.now() + "\"");
    jsonObject.put("jars", new ArrayList<>());
    jarConfigArray.add(jsonObject);
    TestHelper.insertFileContent(jarConfig, jarConfigArray.toJSONString());

    fileService.updateNameInJarConfig(fileToRename, newName);
    Assert.assertTrue(TestHelper.copyFileContent(jarConfig).contains("\"fileName\":\"" + newName + "\""));
    Assert.assertFalse(TestHelper.copyFileContent(jarConfig).contains("\"fileName\":\"" + fileToRename + "\""));
  }

  @Test
  public void updateJarsInJarConfigTest(){
    String fileToUpdate = TestHelper.generateRandomString();
    String jar1 = TestHelper.generateRandomString();
    String jar2 = TestHelper.generateRandomString();
    String jarToAdd = TestHelper.generateRandomString();

    JSONObject jsonObject = new JSONObject();
    jsonObject.put("fileName", fileToUpdate);
    jsonObject.put("jars", List.of(jar1, jar2));
    jsonObject.put("lastUpdated", "\"" + LocalDateTime.now() +"\"");
    jarConfigArray.add(jsonObject);
    TestHelper.insertFileContent(jarConfig, jarConfigArray.toJSONString());

    fileService.updateJarsInJarConfig(fileToUpdate, List.of(jarToAdd));
    Assert.assertTrue(TestHelper.copyFileContent(jarConfig).contains("\"fileName\":\"" + fileToUpdate + "\",\"jars\":[\"" + jarToAdd + "\"]"));
    Assert.assertFalse(TestHelper.copyFileContent(jarConfig).contains("\"fileName\":\"" + fileToUpdate + "\",\"jars\":[\"" + jar1 + "\",\"" + jar2 + "\"]"));
  }

  @Test
  public void getJarsForFileTest(){
    String noJars = TestHelper.generateRandomString();
    String hasJars = TestHelper.generateRandomString();
    String jar1 = TestHelper.generateRandomString();
    String jar2 = TestHelper.generateRandomString();

    TestHelper.insertFileContent(jarConfig, "");
    List<String> result  = fileService.getJarsForFile("empty");
    Assert.assertTrue(result.isEmpty());

    JSONObject jsonObject1 = new JSONObject();
    JSONObject jsonObject2 = new JSONObject();
    jsonObject1.put("fileName", noJars);
    jsonObject1.put("jars", new ArrayList<>());
    jsonObject1.put("lastUpdated", "\"" + LocalDateTime.now() +"\"");
    jarConfigArray.add(jsonObject1);
    jsonObject2.put("fileName", hasJars);
    jsonObject2.put("jars", List.of(jar1, jar2));
    jsonObject2.put("lastUpdated", "\"" + LocalDateTime.now() +"\"");
    jarConfigArray.add(jsonObject2);
    TestHelper.insertFileContent(jarConfig, jarConfigArray.toJSONString());

    result = fileService.getJarsForFile(noJars);
    Assert.assertTrue(result.isEmpty());

    result = fileService.getJarsForFile(hasJars);
    Assert.assertEquals(2, result.size());
    Assert.assertTrue(result.contains(jar1));
    Assert.assertTrue(result.contains(jar2));
  }

}
