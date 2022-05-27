package tests.fileservice;

import com.farkasch.barista.JavaFxApp;
import com.farkasch.barista.gui.codinginterface.CodingInterface;
import com.farkasch.barista.gui.codinginterface.CodingInterfaceContainer;
import com.farkasch.barista.gui.component.ErrorPopup;
import com.farkasch.barista.gui.mainview.sidemenu.SideMenu;
import com.farkasch.barista.services.FileService;
import com.farkasch.barista.services.JavaScriptService;
import com.farkasch.barista.services.PersistenceService;
import com.farkasch.barista.util.FileTemplates;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import javafx.stage.Stage;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.testfx.framework.junit.ApplicationTest;
import tests.testhelper.TestHelper;

@ActiveProfiles("test")
public class MiscFileOperationsTest extends ApplicationTest {

  private FileService fileService;
  private PersistenceService persistenceService;
  private ApplicationContext applicationContext;

  private SideMenu sideMenu;
  private ErrorPopup errorPopup;
  private JavaScriptService javaScriptService;
  private CodingInterfaceContainer codingInterfaceContainer;
  private FileTemplates fileTemplates;

  private HashMap<String, Object> beansToReplace;

  @Override
  public void start(Stage stage) {
    try {
      applicationContext = SpringApplication.run(JavaFxApp.class);
      persistenceService = applicationContext.getBean(PersistenceService.class);
      fileService = applicationContext.getBean(FileService.class);

      beansToReplace = new HashMap<>();

      //mocking all classes inside FileService
      sideMenu = Mockito.mock(SideMenu.class);
      errorPopup = Mockito.mock(ErrorPopup.class);
      javaScriptService = Mockito.spy(new JavaScriptService());
      codingInterfaceContainer = Mockito.mock(CodingInterfaceContainer.class);
      fileTemplates = Mockito.mock(FileTemplates.class);

      beansToReplace.put("sideMenu", sideMenu);
      beansToReplace.put("errorPopup", errorPopup);
      beansToReplace.put("javaScriptService", javaScriptService);
      beansToReplace.put("codingInterfaceContainer", codingInterfaceContainer);
      beansToReplace.put("fileTemplates", fileTemplates);
      Mockito.when(fileTemplates.mainTemplate()).thenCallRealMethod();
      Mockito.when(fileTemplates.createPackage(Mockito.any())).thenReturn("");
      Mockito.when(fileTemplates.createImport(Mockito.any())).thenReturn("");

      fileService.prepareForTesting(beansToReplace);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void saveFileTest() {
    try {
      File saveFileTest = new File("src\\test\\resources\\FileServiceTest\\saveFileTest.txt");
      //testing if saveFile can write into an empty file
      fileService.saveFile(saveFileTest, "This is the saved content!");
      Scanner scanner = new Scanner(saveFileTest);
      StringBuilder content = new StringBuilder();
      while (scanner.hasNextLine()) {
        content.append(scanner.nextLine());
        content.append("\n");
      }
      scanner.close();
      Assert.assertEquals("This is the saved content!\n", content.toString());

      //testing if saveFile overrides the content inside the file
      fileService.saveFile(saveFileTest, "This overrides the previous!");
      scanner = new Scanner(saveFileTest);
      content = new StringBuilder();
      while (scanner.hasNextLine()) {
        content.append(scanner.nextLine());
        content.append("\n");
      }
      scanner.close();
      Assert.assertEquals("This overrides the previous!\n", content.toString());

      FileWriter fw = new FileWriter(saveFileTest);
      fw.close();
    } catch (IOException e) {
      Assert.fail();
    }
  }

  @Test
  public void getDirsAndFilesTest(){
    List<File> result = fileService.getDirsAndFiles(null);
    List<File> actual = Arrays.stream(new File(System.getProperty("user.home")).listFiles(file -> !file.isHidden())).toList();
    Assert.assertArrayEquals(result.toArray(), actual.toArray());

    result = fileService.getDirsAndFiles(new File("src\\test\\resources\\FileServiceTest\\FolderContentTest").getAbsolutePath());
    actual = List.of(
      new File("src\\test\\resources\\FileServiceTest\\FolderContentTest\\folder").getAbsoluteFile(),
      new File("src\\test\\resources\\FileServiceTest\\FolderContentTest\\JavaFile.java").getAbsoluteFile(),
      new File("src\\test\\resources\\FileServiceTest\\FolderContentTest\\TextFile.txt").getAbsoluteFile()
    );
    Assert.assertArrayEquals(result.toArray(), actual.toArray());

    result = fileService.getDirsAndFiles(new File("src\\test\\resources\\FileServiceTest\\FolderEmptyTest").getAbsolutePath());
    Assert.assertTrue(result.isEmpty());
  }

  @Test
  public void getClassLevelVariablesTest(){
    File classVariablesTestFile = new File("src\\test\\resources\\FileServiceTest\\ClassLevelVariables.java").getAbsoluteFile();
    File noClassVariablesTestFile = new File("src\\test\\resources\\FileServiceTest\\NoClassLevelVariables.java").getAbsoluteFile();
    List<String> result = fileService.getClassLevelVariables(TestHelper.copyFileContent(classVariablesTestFile));
    List<String> expected = List.of(
      "public int number;",
      "private String text;",
      "boolean decision;",
      "protected char character;",
      "public File file = new File();"
    );
    Assert.assertArrayEquals(expected.toArray(), result.toArray());

    result = fileService.getClassLevelVariables(TestHelper.copyFileContent(noClassVariablesTestFile));
    Assert.assertTrue(result.isEmpty());
  }

  @Test
  public void getGenerateInsertPositionTest(){
    File testFile = new File("src\\test\\resources\\FileServiceTest\\ClassLevelVariables.java").getAbsoluteFile();
    File noMethodTestFile = new File("src\\test\\resources\\FileServiceTest\\NoMethodTest.java").getAbsoluteFile();
    int randomNumber = (int)(Math.random() * 100);
    Mockito.doReturn(randomNumber).when(javaScriptService).getCursorLine(Mockito.any());
    CodingInterface codingInterface = Mockito.mock(CodingInterface.class);

    int result = fileService.getGenerateInsertPosition(TestHelper.copyFileContent(testFile), codingInterface);
    Assert.assertEquals(4, result);

    result = fileService.getGenerateInsertPosition(TestHelper.copyFileContent(noMethodTestFile), codingInterface);
    Assert.assertEquals(randomNumber - 1, result);
  }

  @Test
  public void folderContainsTest(){
    File fileInside = new File("src\\test\\resources\\FileServiceTest\\ClassLevelVariables.java").getAbsoluteFile();
    File folderInside = new File("src\\test\\resources\\FileServiceTest\\FolderEmptyTest").getAbsoluteFile();
    File fileOutside = new File("src\\test\\java\\tests\\TestHelper.java").getAbsoluteFile();
    File folderOutside = new File("src\\test\\resources\\PersistenceServiceTest").getAbsoluteFile();
    File testFolder = new File("src\\test\\resources\\FileServiceTest").getAbsoluteFile();

    Assert.assertTrue(fileService.folderContains(testFolder.getAbsolutePath(), fileInside.getAbsolutePath()));
    Assert.assertTrue(fileService.folderContains(testFolder.getAbsolutePath(), folderInside.getAbsolutePath()));
    Assert.assertFalse(fileService.folderContains(testFolder.getAbsolutePath(), fileOutside.getAbsolutePath()));
    Assert.assertFalse(fileService.folderContains(testFolder.getAbsolutePath(), folderOutside.getAbsolutePath()));
  }

}
