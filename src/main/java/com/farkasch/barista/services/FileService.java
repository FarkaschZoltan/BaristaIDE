package com.farkasch.barista.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
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

  public void cleanupJarJson(){
    //TODO: implement cleaning up json file after it has been not used in a while
  }

  public void createNewInJarJson(String fileName, String... jars){
    try{
      File jarJsonFile = new File("C:\\Program Files\\BaristaIDE\\JarConfig\\JarConfig.json");
      Scanner scanner = new Scanner(jarJsonFile);
      JSONParser parser = new JSONParser();
      FileWriter writer = new FileWriter(jarJsonFile);
      JSONObject jar = new JSONObject();
      String jsonString = "";

      jar.put("fileName", fileName);
      jar.put("jars", jars);
      jar.put("lastUpdated", LocalDateTime.now());

      while(scanner.hasNextLine()){
        jsonString = jsonString.concat(scanner.nextLine());
      }

      JSONArray array = (JSONArray) parser.parse(jsonString);
      array.add(jar);
      jsonString = JSONArray.toJSONString(array);
      writer.write(jsonString);
    } catch(IOException e){
      e.printStackTrace();
    } catch(ParseException e){
      e.printStackTrace();
    }

  }

  public void updateNameInJarJson(String oldFileName, String newFileName){
    try{
      File jarJsonFile = new File("C:\\Program Files\\BaristaIDE\\config\\JarConfig.json");
      Scanner scanner = new Scanner(jarJsonFile);
      JSONParser parser = new JSONParser();
      FileWriter writer = new FileWriter(jarJsonFile);
      String jsonString = "";

      while(scanner.hasNextLine()){
        jsonString = jsonString.concat(scanner.nextLine());
      }
      JSONArray array = (JSONArray) parser.parse(jsonString);

      for(int i = 0; i < array.size(); i++){
        JSONObject jar = (JSONObject) array.get(i);
        if(jar.get("fileName") == oldFileName){
          jar.put("fileName", newFileName);
          jar.put("lastUpdated", LocalDateTime.now());
          break;
        }
      }

      jsonString = JSONArray.toJSONString(array);
      writer.write(jsonString);
    } catch(IOException e){
      e.printStackTrace();
    } catch(ParseException e){
      e.printStackTrace();
    }
  }

  public void updateJarsInJarJson(String fileName, List<String> jars){
    try{
      File jarJsonFile = new File("C:\\Program Files\\BaristaIDE\\config\\JarConfig.json");
      Scanner scanner = new Scanner(jarJsonFile);
      JSONParser parser = new JSONParser();
      FileWriter writer = new FileWriter(jarJsonFile);
      String jsonString = "";

      while(scanner.hasNextLine()){
        jsonString = jsonString.concat(scanner.nextLine());
      }
      JSONArray array = (JSONArray) parser.parse(jsonString);

      for(int i = 0; i < array.size(); i++){
        JSONObject jar = (JSONObject) array.get(i);
        if(jar.get("fileName") == fileName){
          jar.put("jars", jars);
          jar.put("lastUpdated", LocalDateTime.now());
          break;
        }
      }

      jsonString = JSONArray.toJSONString(array);
      writer.write(jsonString);
      writer.close();
    } catch(IOException e){
      e.printStackTrace();
    } catch(ParseException e){
      e.printStackTrace();
    }
  }

  public JSONObject getJarsForFile(String fileName){
    try{
      File jarJsonFile = new File("C:\\Program Files\\BaristaIDE\\config\\JarConfig.json");
      Scanner scanner = new Scanner(jarJsonFile);
      JSONParser parser = new JSONParser();
      String jsonString = "";

      while(scanner.hasNextLine()){
        jsonString = jsonString.concat(scanner.nextLine());
      }
      JSONArray array = (JSONArray) parser.parse(jsonString);

      for(Object j : array){
        if(((JSONObject) j).get("fileName") == fileName){
          return (JSONObject) j;
        }
      }

    } catch(IOException e){
      e.printStackTrace();
    } catch(ParseException e){
      e.printStackTrace();
    }
    return new JSONObject();
  }
}
