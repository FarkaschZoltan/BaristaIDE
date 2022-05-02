package com.farkasch.barista.services;

import com.farkasch.barista.util.enums.JavacEnum;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class ProcessService {

  @Lazy
  @Autowired
  private FileService fileService;

  public File Compile(String filePath, String fileName) {
    HashMap<JavacEnum, Object> args = new HashMap<>();
    List<String> jarsForFile = fileService.getJarsForFile(filePath + "\\" + fileName);

    if (jarsForFile != null) {
      args.put(JavacEnum.CLASSPATH, jarsForFile);
    }

    File argFile = createArgumentFile(filePath, args);
    File sourceFile = createSourceFile(filePath, Arrays.asList(fileName));

    ProcessBuilder pb = new ProcessBuilder();
    pb.directory(new File(filePath));
    pb.command("cmd", "/C", "javac @" + argFile.getName() + " @" + sourceFile.getName());

    try {
      Process process = pb.start();
      BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
      String line;
      while ((line = reader.readLine()) != null) {
        System.out.println(line);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return argFile;
  }

  public void Run(String filePath, String fileName) {
    File argFile = Compile(filePath, fileName);
    try {
      String[] command = new String[]{"cmd.exe", "/c", "start cmd.exe /k \"java @" + argFile.getName() + " " + fileName + "\""};
      Process process = Runtime.getRuntime().exec(command, null, new File(filePath));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  //Creating arguments for compilation/running
  private File createArgumentFile(String path, HashMap<JavacEnum, Object> args) {
    File file = new File(path + "\\arguments.txt");
    try {
      BufferedWriter writer = new BufferedWriter(new FileWriter(file));
      for (JavacEnum arg : args.keySet()) {
        switch (arg) {
          case D:
            writer.append("-d " + args.get(arg));
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
    }
    return file;
  }

  //Creating the source-file for compilation/running
  private File createSourceFile(String path, List<String> files) {
    File file = new File(path + "\\source.txt");
    try {
      BufferedWriter writer = new BufferedWriter(new FileWriter(file));
      for (String f : files) {
        writer.append(f);
        writer.append("\n");
      }
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return file;
  }

}
