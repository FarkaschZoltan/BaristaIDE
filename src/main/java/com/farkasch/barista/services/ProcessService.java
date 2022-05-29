package com.farkasch.barista.services;

import com.farkasch.barista.gui.component.ErrorPopup;
import com.farkasch.barista.util.BaristaProject;
import com.farkasch.barista.util.Result;
import com.farkasch.barista.util.enums.JavacEnum;
import com.farkasch.barista.util.enums.ResultTypeEnum;
import com.farkasch.barista.util.settings.RunSetting;
import com.google.common.io.Files;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

@Service
public class ProcessService {

  @Lazy
  @Autowired
  private FileService fileService;
  @Lazy
  @Autowired
  private PersistenceService persistenceService;
  @Lazy
  @Autowired
  private ErrorPopup errorPopup;

  public Result CompileFile(String filePath, String fileName) {
    HashMap<JavacEnum, Object> args = new HashMap<>();
    List<String> jarsForFile = fileService.getJarsForFile(filePath + "\\" + fileName);

    if (jarsForFile != null) {
      args.put(JavacEnum.CLASSPATH, jarsForFile);
    }

    return Compile(filePath, Arrays.asList(fileName), args);
  }

  public Result CompileProject(BaristaProject baristaProject) {
    HashMap<JavacEnum, Object> args = new HashMap<>();
    args.put(JavacEnum.CLASSPATH, baristaProject.getJars());
    args.put(JavacEnum.D, baristaProject.getTargetFolder());

    return Compile(baristaProject.getSourceRoot(), baristaProject.getSourceFiles(), args);
  }

  private Result Compile(String sourceDirectory, List<String> files, HashMap<JavacEnum, Object> args) {

    Result compileResult = Result.FAIL();

    if (args.get(JavacEnum.D) != null) {
      File target = new File((String) args.get(JavacEnum.D));
      if (!target.exists()) {
        target.mkdir();
      } else if (persistenceService.getOpenProject() != null) {
        for (File file : target.listFiles()) {
          FileSystemUtils.deleteRecursively(file);
        }
      }
    } else {
      String mainClassPath = sourceDirectory + "\\" + Files.getNameWithoutExtension(files.get(0)) + ".class";
      new File(mainClassPath).delete();
    }

    File argFile = createArgumentFile(sourceDirectory, args);
    File sourceFile = createSourceFile(sourceDirectory, files);
    try {
      String command = "cmd /c \"javac @" + argFile.getName() + " @" + sourceFile.getName() + "\"";
      Process process = Runtime.getRuntime().exec(command, null, new File(sourceDirectory));
      process.waitFor();

      if (persistenceService.getOpenProject() != null) {
        BaristaProject project = persistenceService.getOpenProject();
        String mainClassPath =
          new File(project.getMainFile().getAbsolutePath().replace(project.getSourceRoot(), project.getTargetFolder())).getAbsolutePath()
            .replace(project.getMainFile().getName(), Files.getNameWithoutExtension(project.getMainFile().getPath()) + ".class");
        if (new File(mainClassPath).exists()) {
          compileResult = Result.OK();
        }
      } else {
        String mainClassPath = sourceDirectory + "\\" + Files.getNameWithoutExtension(files.get(0)) + ".class";
        if (new File(mainClassPath).exists()) {
          compileResult = Result.OK();
        }
      }

      sourceFile.delete(); //we have no need for the source and argument files anymore, so we delete them.
      argFile.delete();
      if (compileResult.getResult().equals(ResultTypeEnum.FAIL)) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        StringBuilder errorMessage = new StringBuilder();
        reader.lines().forEach(line -> {
          errorMessage.append("echo " + line);
          errorMessage.append("\n");
        });
        File batch = new File(sourceDirectory + "\\showerror.bat");
        FileWriter writer = new FileWriter(batch, false);
        writer.write("@Echo off\n" + errorMessage + "pause");
        writer.close();

        String errorCommand = "cmd /c start /wait cmd /c " + batch.getName();

        Process errorProcess = Runtime.getRuntime().exec(errorCommand, null, new File(sourceDirectory));
        errorProcess.waitFor();

        batch.delete();
      } else {
        HashMap<JavacEnum, Object> runArgs = new HashMap<>();
        runArgs.put(JavacEnum.CLASSPATH, args.get(JavacEnum.CLASSPATH));
        compileResult = Result.OK("", createArgumentFile(args.get(JavacEnum.D) == null ? sourceDirectory : (String) args.get(JavacEnum.D), runArgs));
      }
    } catch (IOException | InterruptedException e) {
      StringWriter stringWriter = new StringWriter();
      PrintWriter printWriter = new PrintWriter(stringWriter);
      e.printStackTrace(printWriter);
      File errorFile = fileService.createErrorLog(stringWriter.toString());
      String message = e.getClass().equals(IOException.class) ? "Error while trying to compile!" : "Compilation was interrupted!";
      errorPopup.showWindow(Result.ERROR(message, errorFile));

      printWriter.close();
      e.printStackTrace();
    }

    return compileResult;
  }

  public void RunFile(String filePath, String fileName) {
    Result compileResult = CompileFile(filePath, fileName);
    if (compileResult.getResult().equals(ResultTypeEnum.OK)) {
      Run((File) compileResult.getReturnValue(), fileName, filePath, null);
    }
  }

  public void RunProject(RunSetting runSetting) {
    BaristaProject baristaProject = persistenceService.getOpenProject();
    Result compileResult = CompileProject(baristaProject);
    if (compileResult.getResult().equals(ResultTypeEnum.OK)) {
      String mainClassPath = baristaProject.getMainFile().getAbsolutePath().replace(baristaProject.getSourceRoot(), baristaProject.getTargetFolder());
      Run((File) compileResult.getReturnValue(), mainClassPath, baristaProject.getTargetFolder(), runSetting);
    }
  }

  private void Run(File argFile, String mainFile, String sourcePath, RunSetting runSetting) {
    try {
      mainFile = mainFile.replace(sourcePath + "\\", "");
      mainFile = mainFile.replace(".java", "");
      mainFile = mainFile.replace("\\", ".");

      File batch = new File(sourcePath + "\\run.bat");
      FileWriter writer = new FileWriter(batch, false);
      if (runSetting == null || runSetting.getCommand() == null) {
        writer.write("@Echo off\n" + "java @" + argFile.getName() + " " + mainFile + "\n" + "pause");
      } else {
        writer.write("@Echo off\n" + runSetting.getCommand() + "\n" + "pause");
      }
      writer.close();

      String command = "cmd /c start /wait cmd /c " + batch.getName();
      Process process = Runtime.getRuntime().exec(command, null, new File(sourcePath));
      process.waitFor();

      argFile.delete();
      batch.delete();
    } catch (IOException | InterruptedException e) {
      StringWriter stringWriter = new StringWriter();
      PrintWriter printWriter = new PrintWriter(stringWriter);
      e.printStackTrace(printWriter);
      File errorFile = fileService.createErrorLog(stringWriter.toString());
      errorPopup.showWindow(Result.ERROR("Error while trying to run!", errorFile));

      printWriter.close();
      e.printStackTrace();
    }
  }

  public void openCommandPrompt(File path) {
    try {
      if (path == null) {
        path = new File(System.getProperty("user.home"));
      }
      String command = "cmd /k start";
      Process process = Runtime.getRuntime().exec(command, null, path);
    } catch (IOException e) {
      StringWriter stringWriter = new StringWriter();
      PrintWriter printWriter = new PrintWriter(stringWriter);
      e.printStackTrace(printWriter);
      File errorFile = fileService.createErrorLog(stringWriter.toString());
      errorPopup.showWindow(Result.ERROR("Error while tyring to open command prompt!", errorFile));

      printWriter.close();
      e.printStackTrace();
    }
  }

  //Creating arguments for compilation/running
  private File createArgumentFile(String path, HashMap<JavacEnum, Object> args) {
    File file = new File(path + "\\arguments.txt");
    try {
      if (!file.exists()) {
        file.createNewFile();
      }
      BufferedWriter writer = new BufferedWriter(new FileWriter(file));
      for (JavacEnum arg : args.keySet()) {
        switch (arg) {
          case D:
            writer.append("-d \"" + ((String) args.get(arg)).replaceAll("\\\\", "\\\\\\\\") + "\"");
            writer.append("\n");
            break;
          case CLASSPATH:
            writer.append("-cp .;");
            for (String cp : (List<String>) args.get(arg)) {
              writer.append(cp);
              if (((List<String>) args.get(arg)).indexOf(cp) < ((List<String>) args.get(arg)).size() - 1) {
                writer.append(";");
              }
            }
            writer.append("\n");
            break;
          case SOURCEPATH:
            writer.append("-sourcepath " + args.get(arg));
            writer.append("\n");
            break;
        }
      }
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
      StringWriter stringWriter = new StringWriter();
      PrintWriter printWriter = new PrintWriter(stringWriter);
      e.printStackTrace(printWriter);
      File errorFile = fileService.createErrorLog(stringWriter.toString());
      errorPopup.showWindow(Result.ERROR("Error while creating argument file!", errorFile));

      printWriter.close();
    }
    return file;
  }

  //Creating the source-file for compilation/running
  private File createSourceFile(String path, List<String> files) {
    File file = new File(path + "\\source.txt");
    try {
      file.createNewFile();
      BufferedWriter writer = new BufferedWriter(new FileWriter(file));
      for (String f : files) {
        writer.append(f.replace(path + "\\", ""));
        writer.append("\n");
      }
      writer.close();
    } catch (IOException e) {
      StringWriter stringWriter = new StringWriter();
      PrintWriter printWriter = new PrintWriter(stringWriter);
      e.printStackTrace(printWriter);
      File errorFile = fileService.createErrorLog(stringWriter.toString());
      errorPopup.showWindow(Result.ERROR("Error while creating source file!", errorFile));

      printWriter.close();
      e.printStackTrace();
    }
    return file;
  }

  public void openDocumentation(){
    try{
      Process p = Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler Documentation.pdf", null, new File("src\\main\\resources"));
      p.waitFor();
    } catch(IOException | InterruptedException e){
      StringWriter stringWriter = new StringWriter();
      PrintWriter printWriter = new PrintWriter(stringWriter);
      e.printStackTrace(printWriter);
      File errorFile = fileService.createErrorLog(stringWriter.toString());
      errorPopup.showWindow(Result.ERROR("Error while trying to open documentation!", errorFile));

      printWriter.close();
      e.printStackTrace();
    }
  }

}
