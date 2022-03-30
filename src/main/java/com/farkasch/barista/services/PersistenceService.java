package com.farkasch.barista.services;

import com.farkasch.barista.gui.codinginterface.CodingInterface;
import java.io.File;
import org.springframework.stereotype.Service;

@Service
public class PersistenceService {
  private File activeFile;
  private CodingInterface activeInterface;

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
}
