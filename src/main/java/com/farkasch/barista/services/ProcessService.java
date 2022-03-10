package com.farkasch.barista.services;

import com.farkasch.barista.util.enums.JavacEnum;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import javafx.util.Pair;
import org.springframework.lang.Nullable;

public class ProcessService {

  public static List<String> getDirs(@Nullable String folder) {
    List<String> dirs = new ArrayList<>();

    ProcessBuilder pb = new ProcessBuilder();
    if (folder == null) {
      pb.directory(new File("C:\\Users"));
    } else {
      pb.directory(new File("C:\\Users" + folder));
    }
    pb.command("CMD", "/C", "dir");

    try {
      Process process = pb.start();
      BufferedReader reader = new BufferedReader(
        new InputStreamReader(process.getInputStream()));
      String line;

      while ((line = reader.readLine()) != null) {
        //System.out.println(line);
        List<String> splitLine = Arrays.asList(line.split(" "));
        if (splitLine.contains("<DIR>") && !(splitLine.contains(".") || splitLine.contains(
          ".."))) {
          splitLine = splitLine.stream().filter(s -> !s.equals("")).toList();
          int startIndex = splitLine.indexOf("<DIR>") + 1;
          String folderName = splitLine.get(startIndex);
          for (int i = startIndex + 1; i < splitLine.size(); i++) {
            folderName += " " + splitLine.get(i);
          }
          dirs.add(folderName);
        }
      }

      //dirs.stream().forEach(s -> System.out.println("-> " + s));
    } catch (IOException e) {
      e.printStackTrace();
    }

    return dirs;
  }

  public static List<Pair<String, Boolean>> getDirsAndFiles(@Nullable String folder) {
    List<Pair<String, Boolean>> dirsAndFiles = new ArrayList<>();

    ProcessBuilder pb = new ProcessBuilder();
    if (folder == null) {
      pb.directory(new File("C:\\Users"));
    } else {
      pb.directory(new File("C:\\Users" + folder));
    }
    pb.command("CMD", "/C", "dir");

    try {
      Process process = pb.start();
      BufferedReader reader = new BufferedReader(
        new InputStreamReader(process.getInputStream()));
      String line;

      while ((line = reader.readLine()) != null) {
        System.out.println(line);
        List<String> splitLine = Arrays.asList(line.split(" "));
        if (splitLine.contains("<DIR>") && !(splitLine.contains(".") || splitLine.contains(
          ".."))) {
          splitLine = splitLine.stream().filter(s -> !s.equals("")).toList();
          int startIndex = splitLine.indexOf("<DIR>") + 1;
          String folderName = splitLine.get(startIndex);
          for (int i = startIndex + 1; i < splitLine.size(); i++) {
            folderName += " " + splitLine.get(i);
          }
          dirsAndFiles.add(new Pair<>(folderName, false));
        } else if (splitLine.get(splitLine.size() - 1).matches(".*[.].[a-z0-9]{2,3}")) {
          splitLine = splitLine.stream().filter(s -> !s.equals("")).toList();
          int startIndex = 5;
          String fileName = splitLine.get(startIndex);
          for (int i = startIndex + 1; i < splitLine.size(); i++) {
            fileName += " " + splitLine.get(i);
          }
          dirsAndFiles.add(new Pair<>(fileName, true));
        }
      }

      dirsAndFiles.stream().forEach(s -> System.out.println("-> " + s));
    } catch (IOException e) {
      e.printStackTrace();
    }

    return dirsAndFiles;
  }

  public static void Compile(String filePath, String fileName, @Nullable List<String> packages){
    ProcessBuilder pb = new ProcessBuilder();
    pb.directory(new File(filePath));
    pb.command("cmd", "/C", "javac " + fileName);

    try{
      Process process = pb.start();
      BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
      String line;
      while((line = reader.readLine()) != null){
        System.out.println(line);
      }
    } catch(IOException e){
      e.printStackTrace();
    }
  }

  public static void Run(String filePath, String fileName, @Nullable List<String> packages){
    Compile(filePath, fileName, packages);
    try {
      String[] command = new String[]{"cmd.exe", "/c", "start cmd.exe /k \"java " + fileName +  "\""};
      Process process = Runtime.getRuntime().exec(command, null, new File(filePath));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  //Creating arguments for compilation/running
  private File createArgumentFile(String path, HashMap<JavacEnum, Object> args){
    File file = new File(path + "\\arguments.txt");
    try{
      BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
      for(JavacEnum arg : args.keySet()){
        switch(arg){
          case D:
            writer.append("-d " + args.get(arg));
            writer.append("\n");
            break;
          case CLASSPATH:
            writer.append("-cp ");
            for(String cp : (List<String>)args.get(arg)){
              writer.append(cp);
              if(((List<String>) args.get(arg)).indexOf(cp) < ((List<String>) args.get(arg)).size() - 1)
                writer.append(";");
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
    } catch(IOException e){
      e.printStackTrace();
    }
    return file;
  }

  //Creating the source-file for compilation/running
  private File createSourceFile(String path, List<String> files){
    File file = new File(path);
    try{
      BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
      for(String f : files){
        writer.append("\"" + file + "\"");
        writer.append("\n");
      }
      writer.close();
    } catch(IOException e){
      e.printStackTrace();
    }
    return file;
  }

}
