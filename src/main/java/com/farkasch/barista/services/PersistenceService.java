package com.farkasch.barista.services;

import com.farkasch.barista.gui.codinginterface.CodingInterface;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.springframework.stereotype.Service;

@Service
public class PersistenceService {

  private File activeFile;
  private List<File> openFiles;
  private List<File> mainFiles;
  private CodingInterface activeInterface;

  public PersistenceService(){
    openFiles = new ArrayList<>();
  }

  public File getActiveFile() {
    return activeFile;
  }

  public void setActiveFile(File activeFile) {
    this.activeFile = activeFile;
  }

  public CodingInterface getActiveInterface() {
    return activeInterface;
  }

  public void setActiveInterface(CodingInterface activeInterface) {
    this.activeInterface = activeInterface;
  }

  public List<File> getOpenFiles() {
    return openFiles;
  }

  public void setOpenFiles(List<File> openFiles) {
    this.openFiles = openFiles;
  }

  public void addOpenFile(File file){
    openFiles.add(file);
  }

  public void removeOpenFile(File file){
    openFiles.remove(file);
  }

  public List<File> getMainFiles() {
    return mainFiles;
  }

  public void setMainFiles(List<File> mainFiles) {
    this.mainFiles = mainFiles;
  }

  public void addMainFile(File file){
    mainFiles.add(file);
  }

  public void removeMainFile(File file){
    mainFiles.remove(file);
  }

  public List<File> updateAndGetCurrentMainFiles(){
    String mainString = "publicstaticvoidmain(String[]args)";
    List<File> newMainFiles = new ArrayList<>();
    for(File f : openFiles){
      try{
        String fileContent = "";
        Scanner scanner = new Scanner(f);
        while(scanner.hasNextLine()){
          fileContent = fileContent.concat(scanner.nextLine());
        }
        System.out.println(fileContent);
        if(fileContent.replaceAll("\\s", "").contains(mainString)){
          newMainFiles.add(f);
        }
      } catch (FileNotFoundException e){
        e.printStackTrace();
      }
    }
    setMainFiles(newMainFiles);
    return newMainFiles;
  }
}
