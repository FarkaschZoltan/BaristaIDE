package com.farkasch.barista.gui.codinginterface;

import com.farkasch.barista.services.PersistenceService;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.layout.BorderPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component
public class CodingInterfaceContainer extends BorderPane {

  private List<CodingInterface> interfaces;
  private int activeInterfaceInd = 0;

  @Autowired
  private ApplicationContext applicationContext;

  @Autowired
  private PersistenceService persistenceService;

  public CodingInterfaceContainer() {
    interfaces = new ArrayList<>();
  }

  public void openFile(File file) {
    CodingInterface activeInterface;
    if (interfaces.isEmpty()) {
      activeInterface = applicationContext.getBean(CodingInterface.class);
      activeInterface.setParent(this);
      newInterface(activeInterface);
    } else {
      activeInterface = interfaces.get(activeInterfaceInd);
    }
    activeInterface.showFile(file);
  }

  public void newInterface(CodingInterface newInterface) {
    interfaces.add(newInterface);
    rearrangeInterfaces(true);
  }

  public void closeInterface(CodingInterface interfaceToClose) {
    interfaces.remove(interfaceToClose);
    rearrangeInterfaces(false);
  }

  private void rearrangeInterfaces(boolean isNew) {
    //Only 3 interfaces allowed at this time
    CodingInterface codingInterface = isNew ? interfaces.get(interfaces.size() - 1) : null;
    if (interfaces.size() == 0) {
      setCenter(null);
    } else if (interfaces.size() == 1 || interfaces.size() == 3) {
      if (isNew) {
        setCenter(codingInterface);
      } else {
        if (getLeft() == null) {
          setCenter(getRight());
          setRight(null);
        } else {
          setCenter(getLeft());
          setLeft(null);
        }
      }
    } else if (interfaces.size() == 2) {
      if (isNew) {
        CodingInterface centerInterface = (CodingInterface) getCenter();
        setLeft(centerInterface);
        setRight(codingInterface);
      } else {
        if (getRight() == null) {
          setRight(getCenter());
          setCenter(null);
        } else if (getLeft() == null) {
          setLeft(getCenter());
          setCenter(null);
        }
      }

    }
  }
}
