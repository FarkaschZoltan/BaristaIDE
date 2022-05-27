package tests.fileservice;

import com.farkasch.barista.JavaFxApp;
import com.farkasch.barista.gui.codinginterface.CodingInterfaceContainer;
import com.farkasch.barista.gui.component.ErrorPopup;
import com.farkasch.barista.gui.component.SimpleDropdown;
import com.farkasch.barista.gui.mainview.sidemenu.SideMenu;
import com.farkasch.barista.services.FileService;
import com.farkasch.barista.services.JavaScriptService;
import com.farkasch.barista.services.PersistenceService;
import com.farkasch.barista.util.FileTemplates;
import com.farkasch.barista.util.Result;
import com.farkasch.barista.util.enums.ResultTypeEnum;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.testfx.framework.junit.ApplicationTest;

@ActiveProfiles("test")
public class OutsideProjectOperationsTest extends ApplicationTest {

  private FileService fileService;
  private PersistenceService persistenceService;
  private ApplicationContext applicationContext;

  private SideMenu sideMenu;
  private ErrorPopup errorPopup;
  private JavaScriptService javaScriptService;
  private CodingInterfaceContainer codingInterfaceContainer;
  private FileTemplates fileTemplates;

  private File file1;
  private File folder1;
  private HashMap<String, Object> beansToReplace;
  private File createFileTest;


  @Override
  public void start(Stage stage) {
    try {
      applicationContext = SpringApplication.run(JavaFxApp.class);
      persistenceService = applicationContext.getBean(PersistenceService.class);
      fileService = applicationContext.getBean(FileService.class);

      beansToReplace = new HashMap<>();
      file1 = new File("src\\test\\resources\\FileServiceTest\\testFile1.txt");
      file1.createNewFile();
      folder1 = new File("src\\test\\resources\\FileServiceTest\\testFolder1");
      folder1.mkdir();
      createFileTest = new File("src\\test\\resources\\FileServiceTest\\createFileTest.txt");

      //mocking all classes inside FileService
      sideMenu = Mockito.mock(SideMenu.class);
      Mockito.when(sideMenu.getOpenFiles()).thenReturn(Mockito.mock(SimpleDropdown.class));
      Mockito.when(sideMenu.getRecentlyClosed()).thenReturn(Mockito.mock(SimpleDropdown.class));
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
    } catch (IOException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  @After
  public void cleanup() {
    file1.delete();
    folder1.delete();
    createFileTest.delete();
  }

  @Test
  public void createFileTest(){
    //testing, if a file is created with createFile
    Result okResult = fileService.createFile(createFileTest.getAbsolutePath());
    Assert.assertTrue(createFileTest.exists());
    Assert.assertEquals(ResultTypeEnum.OK, okResult.getResult());
    Assert.assertEquals(createFileTest.getAbsoluteFile(), okResult.getReturnValue());

    //testing, that if the file already exists, createFile fails
    Result failResult = fileService.createFile(file1.getAbsolutePath());
    Assert.assertEquals(ResultTypeEnum.FAIL, failResult.getResult());
    Assert.assertEquals("A file with this name already exists!", failResult.getMessage());
  }

  @Test
  public void deleteFileTest(){
    //testing, that the file is deleted
    boolean deleteResult = fileService.deleteFile(file1, false);
    Assert.assertTrue(deleteResult);
    Assert.assertFalse(file1.exists());

    //testing, that if the file does not exist, deleteFile fails
    deleteResult = fileService.deleteFile(file1, false);
    Assert.assertFalse(deleteResult);
  }

  @Test
  public void renameFileTest(){
    File renamedFile = new File(file1.getParent() + "\\renamedFile").getAbsoluteFile();
    Result result = fileService.renameFile(file1, "renamedFile", null);
    Assert.assertFalse(file1.exists());
    Assert.assertTrue(renamedFile.exists());
    Assert.assertEquals(ResultTypeEnum.OK, result.getResult());
    Assert.assertEquals(renamedFile, result.getReturnValue());
    renamedFile.delete();
  }

}
