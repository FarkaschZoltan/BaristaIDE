package com.farkasch.barista.gui.codinginterface;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.layout.BorderPane;

public class CodingInterfaceContainer extends BorderPane {

    private List<CodingInterface> interfaces;
    private int activeInterfaceInd = 0;

    public CodingInterface getActiveInterface() {
        return interfaces.get(activeInterfaceInd);
    }

    public CodingInterfaceContainer() {
        interfaces = new ArrayList<>();
    }

    public void openFile(File file) {
        CodingInterface activeInterface;
        if (interfaces.isEmpty()) {
            activeInterface = new CodingInterface();
            newInterface(activeInterface);
        } else {
            activeInterface = interfaces.get(activeInterfaceInd);
        }
        activeInterface.showFile(file);

    }

    public void newInterface(CodingInterface newInterface) {
        //Only 3 interfaces allowed at this time
        if (interfaces.size() == 0 || interfaces.size() == 2) {
            setCenter(newInterface);
        } else if (interfaces.size() == 1) {
            CodingInterface centerInterface = (CodingInterface) getCenter();
            setLeft(centerInterface);
            setRight(newInterface);
        }
        interfaces.add(newInterface);
    }
}
