package com.farkasch.barista.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FileService {

  public void saveFile(File file, String content) {
    try {
      FileOutputStream fos = new FileOutputStream(file, false);
      fos.write(content.getBytes());
      fos.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public File createFile(String path) throws FileAlreadyExistsException {
    File newFile = new File(path);
    try {
      newFile.createNewFile();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return newFile;
  }

  public void cleanupJarJson() {
    //TODO: implement cleaning up json file after it hasn't been used in a while
  }

  public void createNewInJarJson(String fileName, String... jars) {
    try {
      File jarJsonFile = new File("C:\\Program Files\\BaristaIDE\\config\\JarConfig.json");
      Scanner scanner = new Scanner(jarJsonFile);
      JSONParser parser = new JSONParser();
      JSONObject jar = new JSONObject();
      JSONArray array = new JSONArray();
      String jsonString = "";

      while (scanner.hasNextLine()) {
        jsonString = jsonString.concat(scanner.nextLine());
      }

      if (jsonString != "") {
        array = ((JSONArray) parser.parse(jsonString));
      }

      for (Object json : array) {
        if (((JSONObject) json).get("fileName").equals(fileName)) {
          return;
        }
      }

      jar.put("fileName", fileName);
      jar.put("jars", jars == null ? new ArrayList<>() : jars);
      jar.put("lastUpdated", "\"" + LocalDateTime.now() + "\"");

      array.add(jar);
      jsonString = JSONArray.toJSONString(array);
      FileWriter writer = new FileWriter(jarJsonFile);
      writer.write(jsonString);
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ParseException e) {
      e.printStackTrace();
    }

  }

  public void updateNameInJarJson(String oldFileName, String newFileName) {
    try {
      File jarJsonFile = new File("C:\\Program Files\\BaristaIDE\\config\\JarConfig.json");
      Scanner scanner = new Scanner(jarJsonFile);
      JSONParser parser = new JSONParser();
      FileWriter writer = new FileWriter(jarJsonFile);
      String jsonString = "";

      while (scanner.hasNextLine()) {
        jsonString = jsonString.concat(scanner.nextLine());
      }
      JSONArray array = (JSONArray) parser.parse(jsonString);

      for (int i = 0; i < array.size(); i++) {
        JSONObject jar = (JSONObject) array.get(i);
        if (jar.get("fileName") == oldFileName) {
          jar.put("fileName", newFileName);
          jar.put("lastUpdated", LocalDateTime.now());
          break;
        }
      }

      jsonString = JSONArray.toJSONString(array);
      writer.write(jsonString);
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ParseException e) {
      e.printStackTrace();
    }
  }

  public void updateJarsInJarJson(String fileName, List<String> jars) {
    try {
      File jarJsonFile = new File("C:\\Program Files\\BaristaIDE\\config\\JarConfig.json");
      Scanner scanner = new Scanner(jarJsonFile);
      JSONParser parser = new JSONParser();
      JSONArray array;
      String jsonString = "";

      while (scanner.hasNextLine()) {
        jsonString = jsonString.concat(scanner.nextLine());
      }

      if (jsonString == "") {
        array = new JSONArray();
      } else {
        array = ((JSONArray) parser.parse(jsonString));
      }

      for (int i = 0; i < array.size(); i++) {
        JSONObject jar = (JSONObject) array.get(i);
        System.out.println(jar.get("fileName"));
        System.out.println(fileName);
        if (jar.get("fileName").equals(fileName)) {
          System.out.println("jars: ");
          jars.stream().forEach(System.out::println);
          jar.put("jars", jars);
          jar.put("lastUpdated", "\"" + LocalDateTime.now() + "\"");
          break;
        }
      }

      jsonString = JSONArray.toJSONString(array);
      FileWriter writer = new FileWriter(jarJsonFile);
      writer.write(jsonString);
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ParseException e) {
      e.printStackTrace();
    }
  }

  public List<String> getJarsForFile(String fileName) {
    try {
      File jarJsonFile = new File("C:\\Program Files\\BaristaIDE\\config\\JarConfig.json");
      Scanner scanner = new Scanner(jarJsonFile);
      JSONParser parser = new JSONParser();
      String jsonString = "";

      while (scanner.hasNextLine()) {
        jsonString = jsonString.concat(scanner.nextLine());
      }
      JSONArray array = (JSONArray) parser.parse(jsonString);

      for (Object j : array) {
        if (((JSONObject) j).get("fileName").equals(fileName)) {
          return (ArrayList<String>) ((JSONObject) j).get("jars");
        }
      }

    } catch (IOException e) {
      e.printStackTrace();
    } catch (ParseException e) {
      e.printStackTrace();
    }
    return new ArrayList<String>();
  }
}
