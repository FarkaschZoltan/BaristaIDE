package tests.fileservice;

import com.farkasch.barista.JavaFxApp;
import com.farkasch.barista.gui.codinginterface.CodingInterfaceContainer;
import com.farkasch.barista.gui.component.ErrorPopup;
import com.farkasch.barista.gui.component.FolderDropdown.FolderDropdownItem;
import com.farkasch.barista.gui.mainview.sidemenu.SideMenu;
import com.farkasch.barista.services.FileService;
import com.farkasch.barista.services.JavaScriptService;
import com.farkasch.barista.services.PersistenceService;
import com.farkasch.barista.util.BaristaProject;
import com.farkasch.barista.util.FileTemplates;
import com.farkasch.barista.util.Result;
import com.farkasch.barista.util.TreeNode;
import com.farkasch.barista.util.enums.ResultTypeEnum;
import com.farkasch.barista.util.settings.RunSetting;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javafx.scene.control.Button;
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
import org.springframework.util.FileSystemUtils;
import org.testfx.framework.junit.ApplicationTest;
import tests.testhelper.TestHelper;

@ActiveProfiles("test")
public class ProjectOperationsTest extends ApplicationTest {

  //actually autowired spring beans
  private FileService fileService;
  private PersistenceService persistenceService;
  private ApplicationContext applicationContext;

  //classes to mock
  private SideMenu sideMenu;
  private ErrorPopup errorPopup;
  private JavaScriptService javaScriptService;
  private CodingInterfaceContainer codingInterfaceContainer;
  private FileTemplates fileTemplates;

  //other class variables
  private HashMap<String, Object> beansToReplace;
  private File createTextFileTest;
  private File createJavaFileTest;
  private File globalProjectConfig;
  String globalProjectConfigContent;
  private BaristaProject createTestProject;
  private BaristaProject deleteTestProject;
  private FolderDropdownItem testItem;

  @Override
  public void start(Stage stage) {
    try {
      applicationContext = SpringApplication.run(JavaFxApp.class);
      persistenceService = applicationContext.getBean(PersistenceService.class);
      fileService = applicationContext.getBean(FileService.class);

      beansToReplace = new HashMap<>();
      globalProjectConfig = new File(System.getProperty("user.home") + "\\AppData\\Roaming\\BaristaIDE\\config\\ProjectConfig.json");
      globalProjectConfigContent = TestHelper.copyFileContent(globalProjectConfig);

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
      Mockito.when(fileTemplates.mainTemplate()).thenCallRealMethod();
      Mockito.when(fileTemplates.createPackage(Mockito.any())).thenReturn("");
      Mockito.when(fileTemplates.createImport(Mockito.any())).thenReturn("");

      fileService.prepareForTesting(beansToReplace);

      createTextFileTest = new File("src\\test\\resources\\FileServiceTest\\createFileTest.txt");
      createJavaFileTest = new File("src\\test\\resources\\FileServiceTest\\createFileTest.java");
      createTestProject = TestHelper.initCreateTestProject(sideMenu);
      deleteTestProject = TestHelper.initDeleteTestProject(sideMenu);
      testItem = Mockito.mock(FolderDropdownItem.class);


    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  @After
  public void cleanup() {
    createTextFileTest.delete();
    createJavaFileTest.delete();
    TestHelper.insertFileContent(globalProjectConfig, globalProjectConfigContent);
    TestHelper.clearProjectConfig(deleteTestProject);
    TestHelper.clearProjectConfig(createTestProject);
  }

  @Test
  public void createFileTest() {
    //preparing the testing of projects
    persistenceService.setOpenProject(createTestProject);

    //testing, if a java file is created with createFile inside a project
    Result okResult = fileService.createFile(createTextFileTest.getAbsolutePath(), testItem);
    Assert.assertTrue(createTextFileTest.exists());
    Assert.assertEquals(okResult.getResult(), ResultTypeEnum.OK);
    Assert.assertEquals(okResult.getReturnValue(), createTextFileTest.getAbsoluteFile());
    Assert.assertFalse(createTestProject.getSourceFiles().contains(createTextFileTest.getAbsolutePath()));
    Assert.assertTrue(createTestProject.getOtherFiles().contains(createTextFileTest.getAbsolutePath()));

    //testing, if a txt file is created with createFile inside a project
    okResult = fileService.createFile(createJavaFileTest.getAbsolutePath(), testItem);
    Assert.assertTrue(createJavaFileTest.exists());
    Assert.assertEquals(okResult.getResult(), ResultTypeEnum.OK);
    Assert.assertEquals(okResult.getReturnValue(), createJavaFileTest.getAbsoluteFile());
    Assert.assertTrue(createTestProject.getSourceFiles().contains(createJavaFileTest.getAbsolutePath()));
    Assert.assertFalse(createTestProject.getOtherFiles().contains(createJavaFileTest.getAbsolutePath()));

    //testing, that if the file already exists, createFile fails
    Result failResult = fileService.createFile(createJavaFileTest.getAbsolutePath());
    Assert.assertEquals(failResult.getResult(), ResultTypeEnum.FAIL);
    Assert.assertEquals(failResult.getMessage(), "A file with this name already exists!");
  }

  @Test
  public void createFolderTest() {
    //preparing the testing of projects
    persistenceService.setOpenProject(createTestProject);

    //testing, if createFolder creates the folder inside the project
    File createFolderTest = new File("src\\test\\resources\\FileServiceTest\\createTestProject\\src\\main\\java\\createFolderTest");
    Result okResult = fileService.createFolder(createFolderTest.getAbsolutePath(), testItem);
    Assert.assertTrue(createFolderTest.exists());
    Assert.assertEquals(okResult.getResult(), ResultTypeEnum.OK);
    Assert.assertEquals(okResult.getReturnValue(), createFolderTest.getAbsoluteFile());
    Assert.assertTrue(createTestProject.getFolders().contains(createFolderTest.getAbsolutePath()));

    //testing, if the folder already exists, createFolder fails
    Result failResult = fileService.createFolder(createFolderTest.getAbsolutePath(), testItem);
    Assert.assertEquals(failResult.getResult(), ResultTypeEnum.FAIL);
    Assert.assertEquals(failResult.getMessage(), "A folder with this name already exists!");

    createFolderTest.delete();
  }

  @Test
  public void deleteFileTest() {
    persistenceService.setOpenProject(deleteTestProject);
    File textFileToDelete = new File(deleteTestProject.getSourceRoot() + "\\folderToDelete\\textFileToDelete.txt");
    File javaFileToDelete = new File(deleteTestProject.getSourceRoot() + "\\folderToDelete\\javaFileToDelete.java");
    File mainFile = deleteTestProject.getMainFile();
    File folderToDelete = new File(deleteTestProject.getSourceRoot() + "\\folderToDelete");
    Mockito.when(sideMenu.getCompileButton()).thenReturn(new Button());
    Mockito.when(sideMenu.getRunButton()).thenReturn(new Button());

    boolean deleteResult = fileService.deleteFile(textFileToDelete, true);
    Assert.assertTrue(deleteResult);
    Assert.assertFalse(textFileToDelete.exists());
    Assert.assertFalse(deleteTestProject.getOtherFiles().contains(textFileToDelete.getAbsolutePath()));

    deleteResult = fileService.deleteFile(javaFileToDelete, true);
    Assert.assertTrue(deleteResult);
    Assert.assertFalse(javaFileToDelete.exists());
    Assert.assertFalse(deleteTestProject.getSourceFiles().contains(javaFileToDelete.getAbsolutePath()));

    deleteResult = fileService.deleteFile(mainFile, true);
    Assert.assertTrue(deleteResult);
    Assert.assertFalse(mainFile.exists());
    Assert.assertFalse(deleteTestProject.getSourceFiles().contains(mainFile.getAbsolutePath()));
    Assert.assertNotEquals(deleteTestProject.getMainFile(), mainFile);

    deleteResult = fileService.deleteFile(mainFile, false);
    Assert.assertFalse(deleteResult);

    deleteResult = fileService.deleteFile(folderToDelete, true);
    Assert.assertFalse(deleteResult);
    Assert.assertTrue(folderToDelete.exists());
    Assert.assertTrue(deleteTestProject.getFolders().contains(folderToDelete.getAbsolutePath()));
  }

  @Test
  public void deleteFolderTest() {
    persistenceService.setOpenProject(deleteTestProject);
    File textFileToDelete = new File(deleteTestProject.getSourceRoot() + "\\folderToDelete\\textFileToDelete.txt");
    File javaFileToDelete = new File(deleteTestProject.getSourceRoot() + "\\folderToDelete\\javaFileToDelete.java");
    File folderToDelete = new File(deleteTestProject.getSourceRoot() + "\\folderToDelete");

    Mockito.when(testItem.getPath()).thenReturn(folderToDelete.getAbsolutePath());

    //testing, if the folder and all of its contents are deleted from the project
    boolean deleteResult = fileService.deleteFolder(testItem);
    Assert.assertTrue(deleteResult);
    Assert.assertFalse(folderToDelete.exists());
    Assert.assertFalse(textFileToDelete.exists());
    Assert.assertFalse(javaFileToDelete.exists());
    Assert.assertFalse(deleteTestProject.getFolders().contains(folderToDelete.getAbsolutePath()));
    Assert.assertFalse(deleteTestProject.getSourceRoot().contains(javaFileToDelete.getAbsolutePath()));
    Assert.assertFalse(deleteTestProject.getOtherFiles().contains(textFileToDelete.getAbsolutePath()));

    //testing, if the folder does not exist, deletion fails
    deleteResult = fileService.deleteFolder(testItem);
    Assert.assertFalse(deleteResult);
  }

  @Test
  public void createNewProjectTest() {
    BaristaProject testCreation = new BaristaProject("testCreate", new File("src\\test\\resources\\FileServiceTest\\testCreate").getAbsolutePath(),
      false, false);
    fileService.createNewProject(testCreation);
    Assert.assertTrue(new File(testCreation.getProjectRoot()).exists());
    Assert.assertTrue(new File(testCreation.getSourceRoot()).exists());
    Assert.assertTrue(new File(testCreation.getTargetFolder()).exists());
    Assert.assertTrue(testCreation.getMainFile().exists());
    Assert.assertTrue(new File(testCreation.getProjectRoot() + "\\.barista").exists());
    Assert.assertTrue(new File(testCreation.getProjectRoot() + "\\.barista\\ProjectConfig.json").exists());
    Assert.assertTrue(new File(testCreation.getProjectRoot() + "\\.barista\\RunConfig.json").exists());

    FileSystemUtils.deleteRecursively(new File("src\\test\\resources\\FileServiceTest\\testCreate"));
  }

  @Test
  public void loadProjectTest() {
    BaristaProject loadedProject = new BaristaProject(createTestProject.getProjectName(), createTestProject.getProjectRoot());

    fileService.loadProject(loadedProject);
    Assert.assertEquals(loadedProject.getProjectName(), "createTestProject");
    Assert.assertEquals(loadedProject.getProjectRoot(), new File("src\\test\\resources\\FileServiceTest\\createTestProject").getAbsolutePath());
    Assert.assertEquals(loadedProject.getSourceRoot(),
      new File("src\\test\\resources\\FileServiceTest\\createTestProject\\src\\main\\java").getAbsolutePath());
    Assert.assertEquals(loadedProject.getTargetFolder(),
      new File("src\\test\\resources\\FileServiceTest\\createTestProject\\target").getAbsolutePath());
    Assert.assertEquals(loadedProject.getMainFile(),
      new File("src\\test\\resources\\FileServiceTest\\createTestProject\\src\\main\\java\\Main.java").getAbsoluteFile());
    Assert.assertTrue(loadedProject.getJars().isEmpty());
    Assert.assertEquals(loadedProject.getSourceFiles().size(), 1);
    Assert.assertTrue(loadedProject.getSourceFiles()
      .contains(new File("src\\test\\resources\\FileServiceTest\\createTestProject\\src\\main\\java\\Main.java").getAbsolutePath()));
    Assert.assertEquals(1, loadedProject.getOtherFiles().size());
    Assert.assertEquals(loadedProject.getFolders().size(), 7);
    Assert.assertTrue(loadedProject.getFolders().contains(new File("src\\test\\resources\\FileServiceTest\\createTestProject").getAbsolutePath()));
    Assert.assertTrue(
      loadedProject.getFolders().contains(new File("src\\test\\resources\\FileServiceTest\\createTestProject\\.barista").getAbsolutePath()));
    Assert.assertTrue(
      loadedProject.getFolders().contains(new File("src\\test\\resources\\FileServiceTest\\createTestProject\\src").getAbsolutePath()));
    Assert.assertTrue(
      loadedProject.getFolders().contains(new File("src\\test\\resources\\FileServiceTest\\createTestProject\\src\\main").getAbsolutePath()));
    Assert.assertTrue(
      loadedProject.getFolders().contains(new File("src\\test\\resources\\FileServiceTest\\createTestProject\\src\\main\\java").getAbsolutePath()));
    Assert.assertTrue(
      loadedProject.getFolders().contains(new File("src\\test\\resources\\FileServiceTest\\createTestProject\\target").getAbsolutePath()));
    Assert.assertTrue(
      loadedProject.getFolders().contains(new File("src\\test\\resources\\FileServiceTest\\createTestProject\\src\\main\\java\\package").getAbsolutePath()));
    Assert.assertFalse(loadedProject.isMaven());
    Assert.assertFalse(loadedProject.isGradle());
  }

  @Test
  public void saveProjectTest() {
    persistenceService.setOpenProject(createTestProject);
    fileService.saveProject();
    JSONObject config = TestHelper.readIntoJsonObject(new File(createTestProject.getProjectRoot() + "\\.barista\\ProjectConfig.json"));
    Assert.assertEquals(config.get("projectName"), "createTestProject");
    Assert.assertEquals(config.get("projectRoot"), new File("src\\test\\resources\\FileServiceTest\\createTestProject").getAbsolutePath());
    Assert.assertEquals(config.get("sourceRoot"),
      new File("src\\test\\resources\\FileServiceTest\\createTestProject\\src\\main\\java").getAbsolutePath());
    Assert.assertEquals(config.get("targetFolder"),
      new File("src\\test\\resources\\FileServiceTest\\createTestProject\\target").getAbsolutePath());
    Assert.assertEquals(config.get("mainFile"),
      new File("src\\test\\resources\\FileServiceTest\\createTestProject\\src\\main\\java\\Main.java").getAbsolutePath());
    Assert.assertTrue(((ArrayList<String>) config.get("jars")).isEmpty());
    Assert.assertEquals(((ArrayList<String>) config.get("sourceFiles")).size(), 1);
    Assert.assertTrue(((ArrayList<String>) config.get("sourceFiles"))
      .contains(new File("src\\test\\resources\\FileServiceTest\\createTestProject\\src\\main\\java\\Main.java").getAbsolutePath()));
    Assert.assertEquals(1, ((ArrayList<String>) config.get("otherFiles")).size());
    Assert.assertEquals(((ArrayList<String>) config.get("folders")).size(), 7);
    Assert.assertTrue(
      ((ArrayList<String>) config.get("folders")).contains(new File("src\\test\\resources\\FileServiceTest\\createTestProject").getAbsolutePath()));
    Assert.assertTrue(((ArrayList<String>) config.get("folders")).contains(
      new File("src\\test\\resources\\FileServiceTest\\createTestProject\\.barista").getAbsolutePath()));
    Assert.assertTrue(((ArrayList<String>) config.get("folders")).contains(
      new File("src\\test\\resources\\FileServiceTest\\createTestProject\\src").getAbsolutePath()));
    Assert.assertTrue(((ArrayList<String>) config.get("folders")).contains(
      new File("src\\test\\resources\\FileServiceTest\\createTestProject\\src\\main").getAbsolutePath()));
    Assert.assertTrue(((ArrayList<String>) config.get("folders")).contains(
      new File("src\\test\\resources\\FileServiceTest\\createTestProject\\src\\main\\java").getAbsolutePath()));
    Assert.assertTrue(((ArrayList<String>) config.get("folders")).contains(
      new File("src\\test\\resources\\FileServiceTest\\createTestProject\\target").getAbsolutePath()));
    Assert.assertTrue(((ArrayList<String>) config.get("folders")).contains(
      new File("src\\test\\resources\\FileServiceTest\\createTestProject\\src\\main\\java\\package").getAbsolutePath()));
    Assert.assertFalse((boolean) config.get("maven"));
    Assert.assertFalse((boolean) config.get("gradle"));
  }

  @Test
  public void deleteProjectTest() {
    BaristaProject projectToDelete = TestHelper.initProjectToDelete(sideMenu);
    fileService.createNewProject(projectToDelete);
    Assert.assertTrue(new File(projectToDelete.getProjectRoot()).exists());
    Assert.assertTrue(new File(projectToDelete.getSourceRoot()).exists());
    Assert.assertTrue(new File(projectToDelete.getTargetFolder()).exists());
    Assert.assertTrue(projectToDelete.getMainFile().exists());
    Assert.assertTrue(new File(projectToDelete.getProjectRoot() + "\\.barista").exists());
    Assert.assertTrue(new File(projectToDelete.getProjectRoot() + "\\.barista\\ProjectConfig.json").exists());
    Assert.assertTrue(new File(projectToDelete.getProjectRoot() + "\\.barista\\RunConfig.json").exists());
    persistenceService.setOpenProject(projectToDelete);

    fileService.deleteProject(projectToDelete);
    Assert.assertFalse(new File(projectToDelete.getProjectRoot()).exists());
    Assert.assertFalse(new File(projectToDelete.getSourceRoot()).exists());
    Assert.assertFalse(new File(projectToDelete.getTargetFolder()).exists());
    Assert.assertFalse(projectToDelete.getMainFile().exists());
    Assert.assertFalse(new File(projectToDelete.getProjectRoot() + "\\.barista").exists());
    Assert.assertFalse(new File(projectToDelete.getProjectRoot() + "\\.barista\\ProjectConfig.json").exists());
    Assert.assertFalse(new File(projectToDelete.getProjectRoot() + "\\.barista\\RunConfig.json").exists());
  }

  @Test
  public void renameProjectTest() {
    persistenceService.setOpenProject(createTestProject);
    Mockito.when(sideMenu.getProjectFolderDropdown().getRootNode()).thenReturn(Mockito.mock(TreeNode.class));

    fileService.renameProject("renamedProject", testItem);
    Assert.assertEquals(createTestProject.getProjectName(), "renamedProject");
    Assert.assertEquals(createTestProject.getProjectRoot(), new File("src\\test\\resources\\FileServiceTest\\renamedProject").getAbsolutePath());
    Assert.assertEquals(createTestProject.getSourceRoot(),
      new File("src\\test\\resources\\FileServiceTest\\renamedProject\\src\\main\\java").getAbsolutePath());
    Assert.assertEquals(createTestProject.getTargetFolder(),
      new File("src\\test\\resources\\FileServiceTest\\renamedProject\\target").getAbsolutePath());
    Assert.assertEquals(createTestProject.getMainFile(),
      new File("src\\test\\resources\\FileServiceTest\\renamedProject\\src\\main\\java\\Main.java").getAbsoluteFile());
    Assert.assertTrue(createTestProject.getJars().isEmpty());
    Assert.assertEquals(createTestProject.getSourceFiles().size(), 1);
    Assert.assertTrue(createTestProject.getSourceFiles()
      .contains(new File("src\\test\\resources\\FileServiceTest\\renamedProject\\src\\main\\java\\Main.java").getAbsolutePath()));
    Assert.assertEquals(1, createTestProject.getOtherFiles().size());
    Assert.assertEquals(createTestProject.getFolders().size(), 7);
    Assert.assertTrue(createTestProject.getFolders().contains(new File("src\\test\\resources\\FileServiceTest\\renamedProject").getAbsolutePath()));
    Assert.assertTrue(
      createTestProject.getFolders().contains(new File("src\\test\\resources\\FileServiceTest\\renamedProject\\.barista").getAbsolutePath()));
    Assert.assertTrue(
      createTestProject.getFolders().contains(new File("src\\test\\resources\\FileServiceTest\\renamedProject\\src").getAbsolutePath()));
    Assert.assertTrue(
      createTestProject.getFolders().contains(new File("src\\test\\resources\\FileServiceTest\\renamedProject\\src\\main").getAbsolutePath()));
    Assert.assertTrue(
      createTestProject.getFolders().contains(new File("src\\test\\resources\\FileServiceTest\\renamedProject\\src\\main\\java").getAbsolutePath()));
    Assert.assertTrue(
      createTestProject.getFolders().contains(new File("src\\test\\resources\\FileServiceTest\\renamedProject\\target").getAbsolutePath()));
    Assert.assertTrue(
      createTestProject.getFolders().contains(new File("src\\test\\resources\\FileServiceTest\\renamedProject\\src\\main\\java\\package").getAbsolutePath()));
    Assert.assertFalse(createTestProject.isMaven());
    Assert.assertFalse(createTestProject.isGradle());

    JSONObject config = TestHelper.readIntoJsonObject(new File(createTestProject.getProjectRoot() + "\\.barista\\ProjectConfig.json"));
    Assert.assertEquals(config.get("projectName"), "renamedProject");
    Assert.assertEquals(config.get("projectRoot"), new File("src\\test\\resources\\FileServiceTest\\renamedProject").getAbsolutePath());
    Assert.assertEquals(config.get("sourceRoot"),
      new File("src\\test\\resources\\FileServiceTest\\renamedProject\\src\\main\\java").getAbsolutePath());
    Assert.assertEquals(config.get("targetFolder"),
      new File("src\\test\\resources\\FileServiceTest\\renamedProject\\target").getAbsolutePath());
    Assert.assertEquals(config.get("mainFile"),
      new File("src\\test\\resources\\FileServiceTest\\renamedProject\\src\\main\\java\\Main.java").getAbsolutePath());
    Assert.assertTrue(((ArrayList<String>) config.get("jars")).isEmpty());
    Assert.assertEquals(((ArrayList<String>) config.get("sourceFiles")).size(), 1);
    Assert.assertTrue(((ArrayList<String>) config.get("sourceFiles"))
      .contains(new File("src\\test\\resources\\FileServiceTest\\renamedProject\\src\\main\\java\\Main.java").getAbsolutePath()));
    Assert.assertEquals(1, ((ArrayList<String>) config.get("otherFiles")).size());
    Assert.assertEquals(((ArrayList<String>) config.get("folders")).size(), 7);
    Assert.assertTrue(
      ((ArrayList<String>) config.get("folders")).contains(new File("src\\test\\resources\\FileServiceTest\\renamedProject").getAbsolutePath()));
    Assert.assertTrue(((ArrayList<String>) config.get("folders")).contains(
      new File("src\\test\\resources\\FileServiceTest\\renamedProject\\.barista").getAbsolutePath()));
    Assert.assertTrue(
      ((ArrayList<String>) config.get("folders")).contains(new File("src\\test\\resources\\FileServiceTest\\renamedProject\\src").getAbsolutePath()));
    Assert.assertTrue(((ArrayList<String>) config.get("folders")).contains(
      new File("src\\test\\resources\\FileServiceTest\\renamedProject\\src\\main").getAbsolutePath()));
    Assert.assertTrue(((ArrayList<String>) config.get("folders")).contains(
      new File("src\\test\\resources\\FileServiceTest\\renamedProject\\src\\main\\java").getAbsolutePath()));
    Assert.assertTrue(((ArrayList<String>) config.get("folders")).contains(
      new File("src\\test\\resources\\FileServiceTest\\renamedProject\\target").getAbsolutePath()));
    Assert.assertTrue(((ArrayList<String>) config.get("folders")).contains(
      new File("src\\test\\resources\\FileServiceTest\\renamedProject\\src\\main\\java\\package").getAbsolutePath()));
    Assert.assertFalse((boolean) config.get("maven"));
    Assert.assertFalse((boolean) config.get("gradle"));

    fileService.renameProject("createTestProject", testItem);
    Assert.assertEquals(createTestProject.getProjectName(), "createTestProject");
    Assert.assertEquals(createTestProject.getProjectRoot(), new File("src\\test\\resources\\FileServiceTest\\createTestProject").getAbsolutePath());
    Assert.assertEquals(createTestProject.getSourceRoot(),
      new File("src\\test\\resources\\FileServiceTest\\createTestProject\\src\\main\\java").getAbsolutePath());
    Assert.assertEquals(createTestProject.getTargetFolder(),
      new File("src\\test\\resources\\FileServiceTest\\createTestProject\\target").getAbsolutePath());
    Assert.assertEquals(createTestProject.getMainFile(),
      new File("src\\test\\resources\\FileServiceTest\\createTestProject\\src\\main\\java\\Main.java").getAbsoluteFile());
    Assert.assertTrue(createTestProject.getJars().isEmpty());
    Assert.assertEquals(createTestProject.getSourceFiles().size(), 1);
    Assert.assertTrue(createTestProject.getSourceFiles()
      .contains(new File("src\\test\\resources\\FileServiceTest\\createTestProject\\src\\main\\java\\Main.java").getAbsolutePath()));
    Assert.assertEquals(1, createTestProject.getOtherFiles().size());
    Assert.assertEquals(createTestProject.getFolders().size(), 7);
    Assert.assertTrue(
      createTestProject.getFolders().contains(new File("src\\test\\resources\\FileServiceTest\\createTestProject").getAbsolutePath()));
    Assert.assertTrue(
      createTestProject.getFolders().contains(new File("src\\test\\resources\\FileServiceTest\\createTestProject\\.barista").getAbsolutePath()));
    Assert.assertTrue(
      createTestProject.getFolders().contains(new File("src\\test\\resources\\FileServiceTest\\createTestProject\\src").getAbsolutePath()));
    Assert.assertTrue(
      createTestProject.getFolders().contains(new File("src\\test\\resources\\FileServiceTest\\createTestProject\\src\\main").getAbsolutePath()));
    Assert.assertTrue(createTestProject.getFolders()
      .contains(new File("src\\test\\resources\\FileServiceTest\\createTestProject\\src\\main\\java").getAbsolutePath()));
    Assert.assertTrue(
      createTestProject.getFolders().contains(new File("src\\test\\resources\\FileServiceTest\\createTestProject\\target").getAbsolutePath()));
    Assert.assertTrue(
      createTestProject.getFolders().contains(new File("src\\test\\resources\\FileServiceTest\\createTestProject\\src\\main\\java\\package").getAbsolutePath()));
    Assert.assertFalse(createTestProject.isMaven());
    Assert.assertFalse(createTestProject.isGradle());

    config = TestHelper.readIntoJsonObject(new File(createTestProject.getProjectRoot() + "\\.barista\\ProjectConfig.json"));
    Assert.assertEquals(config.get("projectName"), "createTestProject");
    Assert.assertEquals(config.get("projectRoot"), new File("src\\test\\resources\\FileServiceTest\\createTestProject").getAbsolutePath());
    Assert.assertEquals(config.get("sourceRoot"),
      new File("src\\test\\resources\\FileServiceTest\\createTestProject\\src\\main\\java").getAbsolutePath());
    Assert.assertEquals(config.get("targetFolder"),
      new File("src\\test\\resources\\FileServiceTest\\createTestProject\\target").getAbsolutePath());
    Assert.assertEquals(config.get("mainFile"),
      new File("src\\test\\resources\\FileServiceTest\\createTestProject\\src\\main\\java\\Main.java").getAbsolutePath());
    Assert.assertTrue(((ArrayList<String>) config.get("jars")).isEmpty());
    Assert.assertEquals(((ArrayList<String>) config.get("sourceFiles")).size(), 1);
    Assert.assertTrue(((ArrayList<String>) config.get("sourceFiles"))
      .contains(new File("src\\test\\resources\\FileServiceTest\\createTestProject\\src\\main\\java\\Main.java").getAbsolutePath()));
    Assert.assertEquals(1, ((ArrayList<String>) config.get("otherFiles")).size());
    Assert.assertEquals(((ArrayList<String>) config.get("folders")).size(), 7);
    Assert.assertTrue(
      ((ArrayList<String>) config.get("folders")).contains(new File("src\\test\\resources\\FileServiceTest\\createTestProject").getAbsolutePath()));
    Assert.assertTrue(((ArrayList<String>) config.get("folders")).contains(
      new File("src\\test\\resources\\FileServiceTest\\createTestProject\\.barista").getAbsolutePath()));
    Assert.assertTrue(((ArrayList<String>) config.get("folders")).contains(
      new File("src\\test\\resources\\FileServiceTest\\createTestProject\\src").getAbsolutePath()));
    Assert.assertTrue(((ArrayList<String>) config.get("folders")).contains(
      new File("src\\test\\resources\\FileServiceTest\\createTestProject\\src\\main").getAbsolutePath()));
    Assert.assertTrue(((ArrayList<String>) config.get("folders")).contains(
      new File("src\\test\\resources\\FileServiceTest\\createTestProject\\src\\main\\java").getAbsolutePath()));
    Assert.assertTrue(((ArrayList<String>) config.get("folders")).contains(
      new File("src\\test\\resources\\FileServiceTest\\createTestProject\\target").getAbsolutePath()));
    Assert.assertTrue(((ArrayList<String>) config.get("folders")).contains(
      new File("src\\test\\resources\\FileServiceTest\\createTestProject\\src\\main\\java").getAbsolutePath()));
    Assert.assertFalse((boolean) config.get("maven"));
    Assert.assertFalse((boolean) config.get("gradle"));
  }

  @Test
  public void getProjectsTest() {
    TestHelper.insertFileContent(globalProjectConfig, "");
    JSONObject project1 = new JSONObject();
    String projectName1 = TestHelper.generateRandomString();
    String projectRoot1 = TestHelper.generateRandomString();
    JSONObject project2 = new JSONObject();
    String projectName2 = TestHelper.generateRandomString();
    String projectRoot2 = TestHelper.generateRandomString();

    List<BaristaProject> result = fileService.getProjects();
    Assert.assertTrue(result.isEmpty());

    project1.put("projectName", projectName1);
    project1.put("projectRoot", projectRoot1);
    project2.put("projectName", projectName2);
    project2.put("projectRoot", projectRoot2);
    JSONArray array = new JSONArray();
    array.add(project1);
    array.add(project2);
    TestHelper.insertFileContent(globalProjectConfig, array.toJSONString());

    result = fileService.getProjects();
    Assert.assertEquals(result.size(), 2);
    Assert.assertEquals(result.get(0).getProjectName(), projectName1);
    Assert.assertEquals(result.get(0).getProjectRoot(), projectRoot1);
    Assert.assertEquals(result.get(1).getProjectName(), projectName2);
    Assert.assertEquals(result.get(1).getProjectRoot(), projectRoot2);
  }

  @Test
  public void getRunConfigTest() {
    persistenceService.setOpenProject(createTestProject);
    File runConfig = new File(createTestProject.getProjectRoot() + "\\.barista\\RunConfig.json");
    String runSettingName1 = TestHelper.generateRandomString();
    String command1 = TestHelper.generateRandomString();
    JSONObject runSetting1 = new JSONObject();
    String runSettingName2 = TestHelper.generateRandomString();
    String command2 = TestHelper.generateRandomString();
    JSONObject runSetting2 = new JSONObject();
    JSONArray array = new JSONArray();

    runSetting1.put("name", runSettingName1);
    runSetting1.put("command", command1);
    runSetting2.put("name", runSettingName2);
    runSetting2.put("command", command2);
    array.add(runSetting1);
    array.add(runSetting2);

    TestHelper.insertFileContent(runConfig, "");
    List<RunSetting> result = fileService.getRunConfig();
    Assert.assertEquals(result.size(), 1);
    Assert.assertEquals(result.get(0).getName(), createTestProject.getProjectName());
    Assert.assertNull(result.get(0).getCommand());

    TestHelper.insertFileContent(runConfig, array.toJSONString());
    result = fileService.getRunConfig();
    Assert.assertEquals(result.size(), 3);
    Assert.assertEquals(result.get(0).getName(), createTestProject.getProjectName());
    Assert.assertNull(result.get(0).getCommand());
    Assert.assertEquals(result.get(1).getName(), runSettingName1);
    Assert.assertEquals(result.get(1).getCommand(), command1);
    Assert.assertEquals(result.get(2).getName(), runSettingName2);
    Assert.assertEquals(result.get(2).getCommand(), command2);
  }

  @Test
  public void setRunConfigTest() {
    persistenceService.setOpenProject(createTestProject);
    File runConfig = new File(createTestProject.getProjectRoot() + "\\.barista\\RunConfig.json");
    String runSettingName1 = TestHelper.generateRandomString();
    String command1 = TestHelper.generateRandomString();
    RunSetting runSetting1 = new RunSetting(runSettingName1, command1);
    String runSettingName2 = TestHelper.generateRandomString();
    String command2 = TestHelper.generateRandomString();
    RunSetting runSetting2 = new RunSetting(runSettingName2, command2);

    fileService.setRunConfig(List.of(runSetting1, runSetting2));
    JSONArray runConfigContent = TestHelper.readIntoJsonArray(runConfig);
    Assert.assertEquals(runConfigContent.size(), 2);
    Assert.assertEquals(((JSONObject) runConfigContent.get(0)).get("name"), runSettingName1);
    Assert.assertEquals(((JSONObject) runConfigContent.get(0)).get("command"), command1);
    Assert.assertEquals(((JSONObject) runConfigContent.get(1)).get("name"), runSettingName2);
    Assert.assertEquals(((JSONObject) runConfigContent.get(1)).get("command"), command2);
  }

  @Test
  public void renameFileTest() {
    persistenceService.setOpenProject(createTestProject);
    File textFile = new File(createTestProject.getOtherFiles().get(0));
    File mainFile = createTestProject.getMainFile();
    String newTextName = "renamedTextFile.txt";
    String newMainName = "RenamedMainFile.java";
    File newTextFile = new File(textFile.getParent() + "\\" + newTextName);
    File newMainFile = new File(mainFile.getParent() + "\\" + newMainName);

    Result result = fileService.renameFile(textFile, newTextName, testItem);
    JSONObject configContent = TestHelper.readIntoJsonObject(new File(createTestProject.getProjectRoot() + "\\.barista\\ProjectConfig.json"));
    Assert.assertFalse(textFile.exists());
    Assert.assertTrue(newTextFile.exists());
    Assert.assertEquals(ResultTypeEnum.OK, result.getResult());
    Assert.assertEquals(newTextFile, result.getReturnValue());
    Assert.assertTrue(createTestProject.getOtherFiles().contains(newTextFile.getAbsolutePath()));
    Assert.assertFalse(createTestProject.getOtherFiles().contains(textFile.getAbsolutePath()));
    Assert.assertTrue(((ArrayList) configContent.get("otherFiles")).contains(newTextFile.getAbsolutePath()));
    Assert.assertFalse(((ArrayList) configContent.get("otherFiles")).contains(textFile.getAbsolutePath()));

    result = fileService.renameFile(mainFile, newMainName, testItem);
    configContent = TestHelper.readIntoJsonObject(new File(createTestProject.getProjectRoot() + "\\.barista\\ProjectConfig.json"));
    Assert.assertFalse(mainFile.exists());
    Assert.assertTrue(newMainFile.exists());
    Assert.assertEquals(ResultTypeEnum.OK, result.getResult());
    Assert.assertEquals(newMainFile, result.getReturnValue());
    Assert.assertTrue(createTestProject.getSourceFiles().contains(newMainFile.getAbsolutePath()));
    Assert.assertFalse(createTestProject.getSourceFiles().contains(mainFile.getAbsolutePath()));
    Assert.assertEquals(newMainFile, createTestProject.getMainFile());
    Assert.assertTrue(((ArrayList) configContent.get("sourceFiles")).contains(newMainFile.getAbsolutePath()));
    Assert.assertFalse(((ArrayList) configContent.get("sourceFiles")).contains(mainFile.getAbsolutePath()));
    Assert.assertEquals(newMainFile.getAbsolutePath(), configContent.get("mainFile"));

    result = fileService.renameFile(newTextFile, "textFile.txt", testItem);
    configContent = TestHelper.readIntoJsonObject(new File(createTestProject.getProjectRoot() + "\\.barista\\ProjectConfig.json"));
    Assert.assertTrue(textFile.exists());
    Assert.assertFalse(newTextFile.exists());
    Assert.assertEquals(ResultTypeEnum.OK, result.getResult());
    Assert.assertEquals(textFile, result.getReturnValue());
    Assert.assertFalse(createTestProject.getOtherFiles().contains(newTextFile.getAbsolutePath()));
    Assert.assertTrue(createTestProject.getOtherFiles().contains(textFile.getAbsolutePath()));
    Assert.assertFalse(((ArrayList) configContent.get("otherFiles")).contains(newTextFile.getAbsolutePath()));
    Assert.assertTrue(((ArrayList) configContent.get("otherFiles")).contains(textFile.getAbsolutePath()));

    result = fileService.renameFile(newMainFile, "Main.java", testItem);
    configContent = TestHelper.readIntoJsonObject(new File(createTestProject.getProjectRoot() + "\\.barista\\ProjectConfig.json"));
    Assert.assertTrue(mainFile.exists());
    Assert.assertFalse(newMainFile.exists());
    Assert.assertEquals(ResultTypeEnum.OK, result.getResult());
    Assert.assertEquals(mainFile, result.getReturnValue());
    Assert.assertFalse(createTestProject.getSourceFiles().contains(newMainFile.getAbsolutePath()));
    Assert.assertTrue(createTestProject.getSourceFiles().contains(mainFile.getAbsolutePath()));
    Assert.assertEquals(mainFile, createTestProject.getMainFile());
    Assert.assertFalse(((ArrayList) configContent.get("sourceFiles")).contains(newMainFile.getAbsolutePath()));
    Assert.assertTrue(((ArrayList) configContent.get("sourceFiles")).contains(mainFile.getAbsolutePath()));
    Assert.assertEquals(mainFile.getAbsolutePath(), configContent.get("mainFile"));
  }

  @Test
  public void renameFolderTest(){
    persistenceService.setOpenProject(createTestProject);
    Mockito.when(sideMenu.getProjectFolderDropdown().getRootNode()).thenReturn(Mockito.mock(TreeNode.class));

    File oldPackage = new File("src\\test\\resources\\FileServiceTest\\createTestProject\\src\\main\\java\\package").getAbsoluteFile();
    File renamedPackage = new File(oldPackage.getParent() + "\\renamedPackage").getAbsoluteFile();
    File textFile = new File(createTestProject.getOtherFiles().get(0)).getAbsoluteFile();
    File renamedTextFile = new File(renamedPackage.getAbsolutePath() + "\\textFile.txt").getAbsoluteFile();

    Result result = fileService.renameFolder(oldPackage, "renamedPackage", testItem);
    JSONObject configContent = TestHelper.readIntoJsonObject(new File(createTestProject.getProjectRoot() + "\\.barista\\ProjectConfig.json"));
    Assert.assertFalse(oldPackage.exists());
    Assert.assertTrue(renamedPackage.exists());
    Assert.assertFalse(textFile.exists());
    Assert.assertTrue(renamedTextFile.exists());
    Assert.assertEquals(ResultTypeEnum.OK, result.getResult());
    Assert.assertEquals(renamedPackage, result.getReturnValue());
    Assert.assertTrue(createTestProject.getFolders().contains(renamedPackage.getAbsolutePath()));
    Assert.assertFalse(createTestProject.getFolders().contains(oldPackage.getAbsolutePath()));
    Assert.assertTrue(createTestProject.getOtherFiles().contains(renamedTextFile.getAbsolutePath()));
    Assert.assertFalse(createTestProject.getOtherFiles().contains(textFile.getAbsolutePath()));
    Assert.assertTrue(((ArrayList) configContent.get("folders")).contains(renamedPackage.getAbsolutePath()));
    Assert.assertFalse(((ArrayList) configContent.get("folders")).contains(oldPackage.getAbsolutePath()));
    Assert.assertTrue(((ArrayList) configContent.get("otherFiles")).contains(renamedTextFile.getAbsolutePath()));
    Assert.assertFalse(((ArrayList) configContent.get("otherFiles")).contains(textFile.getAbsolutePath()));

    result = fileService.renameFolder(renamedPackage, oldPackage.getName(), testItem);
    configContent = TestHelper.readIntoJsonObject(new File(createTestProject.getProjectRoot() + "\\.barista\\ProjectConfig.json"));
    Assert.assertTrue(oldPackage.exists());
    Assert.assertFalse(renamedPackage.exists());
    Assert.assertTrue(textFile.exists());
    Assert.assertFalse(renamedTextFile.exists());
    Assert.assertEquals(ResultTypeEnum.OK, result.getResult());
    Assert.assertEquals(oldPackage, result.getReturnValue());
    Assert.assertFalse(createTestProject.getFolders().contains(renamedPackage.getAbsolutePath()));
    Assert.assertTrue(createTestProject.getFolders().contains(oldPackage.getAbsolutePath()));
    Assert.assertFalse(createTestProject.getOtherFiles().contains(renamedTextFile.getAbsolutePath()));
    Assert.assertTrue(createTestProject.getOtherFiles().contains(textFile.getAbsolutePath()));
    Assert.assertFalse(((ArrayList) configContent.get("folders")).contains(renamedPackage.getAbsolutePath()));
    Assert.assertTrue(((ArrayList) configContent.get("folders")).contains(oldPackage.getAbsolutePath()));
    Assert.assertFalse(((ArrayList) configContent.get("otherFiles")).contains(renamedTextFile.getAbsolutePath()));
    Assert.assertTrue(((ArrayList) configContent.get("otherFiles")).contains(textFile.getAbsolutePath()));
  }

  @Test
  public void moveFileTest(){
    persistenceService.setOpenProject(createTestProject);
    File textFile = new File(createTestProject.getOtherFiles().get(0));
    File mainFile = createTestProject.getMainFile();
    File baseFolder = new File("src\\test\\resources\\FileServiceTest\\createTestProject\\src\\main\\java").getAbsoluteFile();
    File packageFolder = new File("src\\test\\resources\\FileServiceTest\\createTestProject\\src\\main\\java\\package").getAbsoluteFile();
    File movedTextFile = new File(baseFolder.getAbsolutePath() + "\\textFile.txt").getAbsoluteFile();
    File movedMainFile = new File(packageFolder.getAbsolutePath() + "\\Main.java").getAbsoluteFile();
    File textExistsCheckFile = new File(baseFolder.getAbsolutePath() + "\\textFile.txt");
    File mainFileExistsCheck = new File(packageFolder.getAbsolutePath() + "\\Main.java");


    Result result = fileService.moveFile(textFile, baseFolder.getAbsolutePath());
    JSONObject configContent = TestHelper.readIntoJsonObject(new File(createTestProject.getProjectRoot() + "\\.barista\\ProjectConfig.json"));
    Assert.assertFalse(textFile.exists());
    Assert.assertTrue(movedTextFile.exists());
    Assert.assertEquals(ResultTypeEnum.OK, result.getResult());
    Assert.assertEquals(movedTextFile, result.getReturnValue());
    Assert.assertTrue(createTestProject.getOtherFiles().contains(movedTextFile.getAbsolutePath()));
    Assert.assertFalse(createTestProject.getOtherFiles().contains(textFile.getAbsolutePath()));
    Assert.assertTrue(((ArrayList) configContent.get("otherFiles")).contains(movedTextFile.getAbsolutePath()));
    Assert.assertFalse(((ArrayList) configContent.get("otherFiles")).contains(textFile.getAbsolutePath()));

    result = fileService.moveFile(movedTextFile, packageFolder.getAbsolutePath());
    configContent = TestHelper.readIntoJsonObject(new File(createTestProject.getProjectRoot() + "\\.barista\\ProjectConfig.json"));
    Assert.assertTrue(textFile.exists());
    Assert.assertFalse(movedTextFile.exists());
    Assert.assertEquals(ResultTypeEnum.OK, result.getResult());
    Assert.assertEquals(textFile, result.getReturnValue());
    Assert.assertFalse(createTestProject.getOtherFiles().contains(movedTextFile.getAbsolutePath()));
    Assert.assertTrue(createTestProject.getOtherFiles().contains(textFile.getAbsolutePath()));
    Assert.assertFalse(((ArrayList) configContent.get("otherFiles")).contains(movedTextFile.getAbsolutePath()));
    Assert.assertTrue(((ArrayList) configContent.get("otherFiles")).contains(textFile.getAbsolutePath()));

    result = fileService.moveFile(mainFile, packageFolder.getAbsolutePath());
    configContent = TestHelper.readIntoJsonObject(new File(createTestProject.getProjectRoot() + "\\.barista\\ProjectConfig.json"));
    Assert.assertFalse(mainFile.exists());
    Assert.assertTrue(movedMainFile.exists());
    Assert.assertEquals(ResultTypeEnum.OK, result.getResult());
    Assert.assertEquals(movedMainFile, result.getReturnValue());
    Assert.assertEquals(createTestProject.getMainFile(), movedMainFile);
    Assert.assertTrue(createTestProject.getSourceFiles().contains(movedMainFile.getAbsolutePath()));
    Assert.assertFalse(createTestProject.getSourceFiles().contains(mainFile.getAbsolutePath()));
    Assert.assertTrue(((ArrayList) configContent.get("sourceFiles")).contains(movedMainFile.getAbsolutePath()));
    Assert.assertFalse(((ArrayList) configContent.get("sourceFiles")).contains(mainFile.getAbsolutePath()));
    Assert.assertEquals(movedMainFile.getAbsolutePath(), configContent.get("mainFile"));

    result = fileService.moveFile(movedMainFile, baseFolder.getAbsolutePath());
    configContent = TestHelper.readIntoJsonObject(new File(createTestProject.getProjectRoot() + "\\.barista\\ProjectConfig.json"));
    Assert.assertTrue(mainFile.exists());
    Assert.assertFalse(movedMainFile.exists());
    Assert.assertEquals(ResultTypeEnum.OK, result.getResult());
    Assert.assertEquals(mainFile, result.getReturnValue());
    Assert.assertEquals(createTestProject.getMainFile(), mainFile);
    Assert.assertFalse(createTestProject.getSourceFiles().contains(movedMainFile.getAbsolutePath()));
    Assert.assertTrue(createTestProject.getSourceFiles().contains(mainFile.getAbsolutePath()));
    Assert.assertFalse(((ArrayList) configContent.get("sourceFiles")).contains(movedMainFile.getAbsolutePath()));
    Assert.assertTrue(((ArrayList) configContent.get("sourceFiles")).contains(mainFile.getAbsolutePath()));
    Assert.assertEquals(mainFile.getAbsolutePath(), configContent.get("mainFile"));

    result = fileService.moveFile(textExistsCheckFile, packageFolder.getAbsolutePath());
    Assert.assertEquals(ResultTypeEnum.FAIL, result.getResult());
    Assert.assertEquals("A file with the same name already exists here!", result.getMessage());

    result = fileService.moveFile(mainFileExistsCheck, baseFolder.getAbsolutePath());
    Assert.assertEquals(ResultTypeEnum.FAIL, result.getResult());
    Assert.assertEquals("A file with the same name already exists here!", result.getMessage());
  }

  @Test
  public void moveFolderTest(){
    persistenceService.setOpenProject(createTestProject);
    File targetFolder = new File("src\\test\\resources\\FileServiceTest\\createTestProject\\src\\main").getAbsoluteFile();
    File originalFolder = new File("src\\test\\resources\\FileServiceTest\\createTestProject\\src\\main\\java").getAbsoluteFile();
    File oldFolder = new File(originalFolder.getAbsolutePath() + "\\package").getAbsoluteFile();
    File movedFolder = new File(targetFolder.getAbsolutePath() + "\\package").getAbsoluteFile();
    File oldTextFile = new File(oldFolder.getAbsolutePath() + "\\textFile.txt").getAbsoluteFile();
    File movedTextFile = new File(movedFolder.getAbsolutePath() + "\\textFile.txt").getAbsoluteFile();
    File folderExistsCheck = new File(movedFolder.getAbsolutePath()).getAbsoluteFile();

    Result result = fileService.moveFolder(oldFolder, targetFolder);
    JSONObject configContent = TestHelper.readIntoJsonObject(new File(createTestProject.getProjectRoot() + "\\.barista\\ProjectConfig.json"));
    Assert.assertFalse(oldFolder.exists());
    Assert.assertTrue(movedFolder.exists());
    Assert.assertFalse(oldTextFile.exists());
    Assert.assertTrue(movedTextFile.exists());
    Assert.assertEquals(ResultTypeEnum.OK, result.getResult());
    Assert.assertEquals(movedFolder, result.getReturnValue());
    Assert.assertFalse(createTestProject.getFolders().contains(oldFolder.getAbsolutePath()));
    Assert.assertTrue(createTestProject.getFolders().contains(movedFolder.getAbsolutePath()));
    Assert.assertFalse(createTestProject.getOtherFiles().contains(oldTextFile.getAbsolutePath()));
    Assert.assertTrue(createTestProject.getOtherFiles().contains(movedTextFile.getAbsolutePath()));
    Assert.assertFalse(((ArrayList) configContent.get("folders")).contains(oldFolder.getAbsolutePath()));
    Assert.assertTrue(((ArrayList) configContent.get("folders")).contains(movedFolder.getAbsolutePath()));
    Assert.assertFalse(((ArrayList) configContent.get("otherFiles")).contains(oldTextFile.getAbsolutePath()));
    Assert.assertTrue(((ArrayList) configContent.get("otherFiles")).contains(movedTextFile.getAbsolutePath()));

    result = fileService.moveFolder(movedFolder, originalFolder);
    configContent = TestHelper.readIntoJsonObject(new File(createTestProject.getProjectRoot() + "\\.barista\\ProjectConfig.json"));
    Assert.assertTrue(oldFolder.exists());
    Assert.assertFalse(movedFolder.exists());
    Assert.assertTrue(oldTextFile.exists());
    Assert.assertFalse(movedTextFile.exists());
    Assert.assertEquals(ResultTypeEnum.OK, result.getResult());
    Assert.assertEquals(oldFolder, result.getReturnValue());
    Assert.assertTrue(createTestProject.getFolders().contains(oldFolder.getAbsolutePath()));
    Assert.assertFalse(createTestProject.getFolders().contains(movedFolder.getAbsolutePath()));
    Assert.assertTrue(createTestProject.getOtherFiles().contains(oldTextFile.getAbsolutePath()));
    Assert.assertFalse(createTestProject.getOtherFiles().contains(movedTextFile.getAbsolutePath()));
    Assert.assertTrue(((ArrayList) configContent.get("folders")).contains(oldFolder.getAbsolutePath()));
    Assert.assertFalse(((ArrayList) configContent.get("folders")).contains(movedFolder.getAbsolutePath()));
    Assert.assertTrue(((ArrayList) configContent.get("otherFiles")).contains(oldTextFile.getAbsolutePath()));
    Assert.assertFalse(((ArrayList) configContent.get("otherFiles")).contains(movedTextFile.getAbsolutePath()));

    result = fileService.moveFolder(folderExistsCheck, originalFolder);
    Assert.assertEquals(ResultTypeEnum.FAIL, result.getResult());
    Assert.assertEquals("A folder with this name already exists here!", result.getMessage());
  }
}

