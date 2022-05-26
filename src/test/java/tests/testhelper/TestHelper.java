package tests.testhelper;

import com.farkasch.barista.gui.component.FolderDropdown;
import com.farkasch.barista.gui.mainview.sidemenu.SideMenu;
import com.farkasch.barista.util.BaristaProject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.mockito.Mockito;

public class TestHelper {

  public static void clearProjectConfig(BaristaProject baristaProject) {
    try {
      File config = new File(baristaProject.getProjectRoot() + "\\.barista\\ProjectConfig.json");
      FileWriter fw = new FileWriter(config);
      fw.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static BaristaProject initCreateTestProject(SideMenu sideMenu) {
    BaristaProject openProject = new BaristaProject("createTestProject",
      new File("src\\test\\resources\\FileServiceTest\\createTestProject").getAbsolutePath(),
      false, false);
    openProject.setMainFile(new File(openProject.getSourceRoot() + "\\Main.java"));
    openProject.setFolders(new ArrayList(List.of(
      new File("src\\test\\resources\\FileServiceTest\\createTestProject").getAbsolutePath(),
      new File("src\\test\\resources\\FileServiceTest\\createTestProject\\.barista").getAbsolutePath(),
      new File("src\\test\\resources\\FileServiceTest\\createTestProject\\src").getAbsolutePath(),
      new File("src\\test\\resources\\FileServiceTest\\createTestProject\\src\\main").getAbsolutePath(),
      new File("src\\test\\resources\\FileServiceTest\\createTestProject\\src\\main\\java").getAbsolutePath(),
      new File("src\\test\\resources\\FileServiceTest\\createTestProject\\target").getAbsolutePath(),
      new File("src\\test\\resources\\FileServiceTest\\createTestProject\\src\\main\\java\\package").getAbsolutePath()
    )));
    openProject.addSourceFile(openProject.getMainFile());
    openProject.addOtherFile(new File("src\\test\\resources\\FileServiceTest\\createTestProject\\src\\main\\java\\package\\textFile.txt").getAbsoluteFile());
    Mockito.when(sideMenu.getProjectFolderDropdown()).thenReturn(Mockito.mock(FolderDropdown.class));

    JSONObject projectConfig = new JSONObject();
    projectConfig.put("targetFolder", new File("src\\test\\resources\\FileServiceTest\\createTestProject\\target").getAbsolutePath());
    projectConfig.put("gradle", false);
    projectConfig.put("projectRoot", new File("src\\test\\resources\\FileServiceTest\\createTestProject").getAbsolutePath());
    projectConfig.put("sourceRoot", new File("src\\test\\resources\\FileServiceTest\\createTestProject\\src\\main\\java").getAbsolutePath());
    projectConfig.put("sourceFiles", List.of(
      new File("src\\test\\resources\\FileServiceTest\\createTestProject\\src\\main\\java\\Main.java").getAbsolutePath()
    ));
    projectConfig.put("folders", List.of(
      new File("src\\test\\resources\\FileServiceTest\\createTestProject").getAbsolutePath(),
      new File("src\\test\\resources\\FileServiceTest\\createTestProject\\.barista").getAbsolutePath(),
      new File("src\\test\\resources\\FileServiceTest\\createTestProject\\src").getAbsolutePath(),
      new File("src\\test\\resources\\FileServiceTest\\createTestProject\\src\\main").getAbsolutePath(),
      new File("src\\test\\resources\\FileServiceTest\\createTestProject\\src\\main\\java").getAbsolutePath(),
      new File("src\\test\\resources\\FileServiceTest\\createTestProject\\target").getAbsolutePath(),
      new File("src\\test\\resources\\FileServiceTest\\createTestProject\\src\\main\\java\\package").getAbsolutePath()
    ));
    projectConfig.put("maven", false);
    projectConfig.put("jars", new ArrayList<>());
    projectConfig.put("otherFiles", List.of(
      new File("src\\test\\resources\\FileServiceTest\\createTestProject\\src\\main\\java\\package\\textFile.txt").getAbsolutePath()
    ));
    projectConfig.put("projectName", "createTestProject");
    projectConfig.put("mainFile", new File("src\\test\\resources\\FileServiceTest\\createTestProject\\src\\main\\java\\Main.java").getAbsolutePath());

    File config = new File("src\\test\\resources\\FileServiceTest\\createTestProject\\.barista\\ProjectConfig.json");
    TestHelper.insertFileContent(config, projectConfig.toJSONString());

    return openProject;
  }

  public static BaristaProject initProjectToDelete(SideMenu sideMenu) {
    BaristaProject openProject = new BaristaProject("projectToDelete",
      new File("src\\test\\resources\\FileServiceTest\\projectToDelete").getAbsolutePath(),
      false, false);
    openProject.setMainFile(new File(openProject.getSourceRoot() + "\\Main.java"));
    openProject.setFolders(new ArrayList(List.of(
      new File("src\\test\\resources\\FileServiceTest\\projectToDelete").getAbsolutePath(),
      new File("src\\test\\resources\\FileServiceTest\\projectToDelete\\.barista").getAbsolutePath(),
      new File("src\\test\\resources\\FileServiceTest\\projectToDelete\\src").getAbsolutePath(),
      new File("src\\test\\resources\\FileServiceTest\\projectToDelete\\src\\main").getAbsolutePath(),
      new File("src\\test\\resources\\FileServiceTest\\projectToDelete\\src\\main\\java").getAbsolutePath(),
      new File("src\\test\\resources\\FileServiceTest\\projectToDelete\\target").getAbsolutePath()
    )));
    openProject.addSourceFile(openProject.getMainFile());
    Mockito.when(sideMenu.getProjectFolderDropdown()).thenReturn(Mockito.mock(FolderDropdown.class));

    return openProject;
  }

  public static BaristaProject initDeleteTestProject(SideMenu sideMenu) {
    try {
      BaristaProject openProject = new BaristaProject("deleteTestProject",
        new File("src\\test\\resources\\FileServiceTest\\deleteTestProject").getAbsolutePath(),
        false, false);

      File mainFile = new File(openProject.getSourceRoot() + "\\Main.java");
      mainFile.createNewFile();
      openProject.setMainFile(mainFile.getAbsoluteFile());

      File folderToDelete = new File(openProject.getSourceRoot() + "\\folderToDelete");
      folderToDelete.mkdir();
      openProject.addFolder(folderToDelete.getAbsoluteFile());

      File textFileToDelete = new File(openProject.getSourceRoot() + "\\folderToDelete\\textFileToDelete.txt");
      textFileToDelete.createNewFile();
      openProject.addOtherFile(textFileToDelete.getAbsoluteFile());

      File javaFileToDelete = new File(openProject.getSourceRoot() + "\\folderToDelete\\javaFileToDelete.java");
      javaFileToDelete.createNewFile();
      openProject.addSourceFile(javaFileToDelete.getAbsoluteFile());

      Mockito.when(sideMenu.getProjectFolderDropdown()).thenReturn(Mockito.mock(FolderDropdown.class));

      return openProject;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static String copyFileContent(File file) {
    try {
      StringBuilder originalContent = new StringBuilder();
      Scanner scanner = new Scanner(file);
      while (scanner.hasNextLine()) {
        originalContent.append(scanner.nextLine());
        originalContent.append("\n");
      }
      scanner.close();
      return originalContent.toString().trim();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void insertFileContent(File file, String content) {
    try {
      FileWriter fw = new FileWriter(file);
      fw.write(content);
      fw.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static String generateRandomString() {
    StringBuilder random = new StringBuilder();
    int min = 97; //char code of a
    int max = 122; //char code of z
    int length = 8;
    for (int i = 0; i < length; i++) {
      char c = (char) (((int) (Math.random() * (max - min))) + min);
      random.append(c);
    }
    return random.toString();
  }

  public static JSONArray readIntoJsonArray(File jsonFile) {
    try {
      Scanner scanner = new Scanner(jsonFile);
      JSONParser parser = new JSONParser();
      JSONArray array;
      String jsonString = "";

      while (scanner.hasNextLine()) {
        jsonString = jsonString.concat(scanner.nextLine());
      }
      scanner.close();
      if (jsonString == "") {
        array = new JSONArray();
      } else {
        array = ((JSONArray) parser.parse(jsonString));
      }

      return array;
    } catch (FileNotFoundException | ParseException e) {
      throw new RuntimeException(e);
    }
  }

  public static JSONObject readIntoJsonObject(File jsonFile) {
    try {
      Scanner scanner = new Scanner(jsonFile);
      JSONParser parser = new JSONParser();
      JSONObject jsonObject;
      String jsonString = "";

      while (scanner.hasNextLine()) {
        jsonString = jsonString.concat(scanner.nextLine());
      }
      scanner.close();
      if (jsonString == "") {
        jsonObject = new JSONObject();
      } else {
        jsonObject = ((JSONObject) parser.parse(jsonString));
      }

      return jsonObject;
    } catch (FileNotFoundException | ParseException e) {
      throw new RuntimeException(e);
    }
  }
}
