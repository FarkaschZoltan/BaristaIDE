package com.farkasch.barista.services;

import com.farkasch.barista.gui.component.ErrorPopup;
import com.farkasch.barista.util.BaristaProject;
import com.farkasch.barista.util.Result;
import com.farkasch.barista.util.enums.JavacEnum;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import javafx.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class ProcessService {

  @Lazy
  @Autowired
  private FileService fileService;
  @Lazy
  @Autowired
  private ErrorPopup errorPopup;

  public File CompileFile(String filePath, String fileName) {
    HashMap<JavacEnum, Object> args = new HashMap<>();
    List<String> jarsForFile = fileService.getJarsForFile(filePath + "\\" + fileName);

    if (jarsForFile != null) {
      args.put(JavacEnum.CLASSPATH, jarsForFile);
    }

    return Compile(filePath, Arrays.asList(fileName), args);
  }

  public File CompileProject(BaristaProject baristaProject){
    HashMap<JavacEnum, Object> args = new HashMap<>();
    args.put(JavacEnum.CLASSPATH, baristaProject.getJars());
    args.put(JavacEnum.D, baristaProject.getTargetFolder());

    return Compile(baristaProject.getSourceRoot(), baristaProject.getSourceFiles(), args);
  }

  private File Compile(String sourceDirectory, List<String> files, HashMap<JavacEnum, Object> args){

    File argFile = createArgumentFile(sourceDirectory, args);
    File sourceFile = createSourceFile(sourceDirectory, files);
    try {
      String command = "cmd /c \"javac @" + argFile.getName() + " @" + sourceFile.getName() + "\"";
      Process process = Runtime.getRuntime().exec(command, null, new File(sourceDirectory));
      process.waitFor();
      System.out.println(process.exitValue());
      new BufferedReader(new FileReader(argFile)).lines().forEach(System.out::println);
      new BufferedReader(new InputStreamReader(process.getErrorStream())).lines().forEach(System.out::println);
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

    sourceFile.delete(); //we have no need for the source/argument file anymore, so we delete it
    argFile.delete();

    HashMap<JavacEnum, Object> runArgs = new HashMap<>();
    runArgs.put(JavacEnum.CLASSPATH, args.get(JavacEnum.CLASSPATH));
    return createArgumentFile(args.get(JavacEnum.D) == null ? sourceDirectory : (String) args.get(JavacEnum.D), runArgs);
  }

  public void RunFile(String filePath, String fileName) {
    File runArgs = CompileFile(filePath, fileName);
    Run(runArgs, fileName, filePath);
  }

  public void RunProject(BaristaProject baristaProject){
    File runArgs = CompileProject(baristaProject);
    String mainClassPath = baristaProject.getMainFile().getAbsolutePath().replace(baristaProject.getSourceRoot(), baristaProject.getTargetFolder());
    Run(runArgs, mainClassPath, baristaProject.getTargetFolder());
  }

  private void Run(File argFile, String mainFile, String sourcePath){
    try {
      mainFile = mainFile.replace(sourcePath + "\\", "");
      mainFile = mainFile.replace(".java", "");
      mainFile = mainFile.replace("\\", ".");
      String command = "cmd /c start /wait cmd.exe /k \"java @" + argFile.getName() + " " + mainFile + "\"" ;
      Process process = Runtime.getRuntime().exec(command, null, new File(sourcePath));
      process.waitFor();
      argFile.delete();
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

  //Creating arguments for compilation/running
  private File createArgumentFile(String path, HashMap<JavacEnum, Object> args) {
    File file = new File(path + "\\arguments.txt");
    try {
      if(!file.exists()){
        System.out.println("argfile created: " + file.createNewFile());
      }
      System.out.println(file.getAbsolutePath());
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
      StringWriter stringWriter = new StringWriter();
      PrintWriter printWriter = new PrintWriter(stringWriter);
      e.printStackTrace(printWriter);
      File errorFile = fileService.createErrorLog(stringWriter.toString());
      errorPopup.showWindow(Result.ERROR("Error while creating argument file!", errorFile));

      printWriter.close();
      e.printStackTrace();
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

}
