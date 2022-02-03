package com.farkasch.barista.gui.codinginterface;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.layout.BorderPane;
import org.springframework.lang.Nullable;

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
            activeInterface = new CodingInterface(this);
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
        if(interfaces.size() == 0){
            setCenter(null);
        } else if (interfaces.size() == 1 || interfaces.size() == 3) {
            if (isNew) {
                setCenter(codingInterface);
            } else {
                if(getLeft() == null){
                    setCenter(getRight());
                    setRight(null);
                } else{
                    setCenter(getLeft());
                    setLeft(null);
                }
            }
        } else if (interfaces.size() == 2) {
            if(isNew){
                CodingInterface centerInterface = (CodingInterface) getCenter();
                setLeft(centerInterface);
                setRight(codingInterface);
            } else {
                if(getRight() == null){
                    setRight(getCenter());
                    setCenter(null);
                } else if(getLeft() == null) {
                    setLeft(getCenter());
                    setCenter(null);
                }
            }

        }
    }
}
