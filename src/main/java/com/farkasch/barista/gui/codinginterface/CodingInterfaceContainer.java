package com.farkasch.barista.gui.codinginterface;

import com.farkasch.barista.gui.codinginterface.SwitchMenu.SwitchMenuItem;
import com.farkasch.barista.util.BaristaDragBoard;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class CodingInterfaceContainer extends StackPane {

  @Autowired
  private ApplicationContext applicationContext;
  @Autowired
  private BaristaDragBoard dragBoard;

  private List<CodingInterface> interfaces;
  private CodingInterface activeInterface;
  private HBox contentPane;
  private BorderPane dragOverPane;
  private Rectangle leftDrag;
  private Rectangle rightDrag;

  @PostConstruct
  public void init() {
    interfaces = new ArrayList<>();
    dragOverPane = new BorderPane();
    contentPane = new HBox();
    leftDrag = new Rectangle();
    rightDrag = new Rectangle();

    setMaxWidth(Double.MAX_VALUE);
    getChildren().add(contentPane);

    dragOverPane.setLeft(leftDrag);
    dragOverPane.setRight(rightDrag);

    dragOverPane.maxWidthProperty().bind(widthProperty());
    dragOverPane.maxHeightProperty().bind(heightProperty());

    List<Rectangle> bothDrag = Arrays.asList(leftDrag, rightDrag);
    bothDrag.stream().forEach(drag -> {
      drag.setFill(Color.GREY);
      drag.setOpacity(0);
      drag.widthProperty().bind(widthProperty().divide(2));
      drag.heightProperty().bind(heightProperty());

      drag.setOnDragOver(event -> {
        if (dragBoard.getDraggedItem().getClass().equals(SwitchMenuItem.class)) {
          event.acceptTransferModes(TransferMode.MOVE);
        }
        event.consume();
      });

      drag.setOnDragEntered(event -> {
        drag.setOpacity(0.2);
        event.consume();
      });

      drag.setOnDragExited(event -> {
        drag.setOpacity(0);
        event.consume();
      });
    });

    leftDrag.setOnDragDropped(event -> {
      CodingInterface codingInterface = (CodingInterface) contentPane.getChildren().get(0);

      if (codingInterface != null) {
        codingInterface.showFileWithDrag(((SwitchMenuItem) dragBoard.getDraggedItem()).getFile());
        event.setDropCompleted(true);
      } else {
        event.setDropCompleted(false);
      }
      dragBoard.setDragTarget(codingInterface);
      event.consume();
    });

    rightDrag.setOnDragDropped(event -> {
      CodingInterface codingInterface = null;
      if (interfaces.size() == 1) {
        codingInterface = applicationContext.getBean(CodingInterface.class);
        codingInterface.setParent(this);
        newInterface(codingInterface);
      } else if (interfaces.size() == 2) {
        codingInterface = (CodingInterface) contentPane.getChildren().get(1);
      }

      if (codingInterface != null) {
        codingInterface.showFileWithDrag(((SwitchMenuItem) dragBoard.getDraggedItem()).getFile());
        event.setDropCompleted(true);
      } else {
        event.setDropCompleted(false);
      }
      dragBoard.setDragTarget(codingInterface);
      event.consume();
    });
  }

  public void openFile(File file) {
    if (interfaces.isEmpty()) {
      CodingInterface newInterface = applicationContext.getBean(CodingInterface.class);
      newInterface.setParent(this);
      newInterface(newInterface);
    }
    activeInterface.showFileWithClick(file);
  }

  public void newInterface(CodingInterface newInterface) {
    interfaces.add(newInterface);
    activeInterface = newInterface;
    contentPane.getChildren().add(newInterface);
  }

  public void closeInterface(CodingInterface interfaceToClose) {
    int InterfaceInd = interfaces.indexOf(interfaceToClose);
    if(interfaces.size() > 1){
      if(InterfaceInd == 0){
        activeInterface = interfaces.get(InterfaceInd + 1);
      } else {
        activeInterface = interfaces.get(InterfaceInd - 1);
      }
    } else {
      activeInterface = null;
    }
    interfaces.remove(interfaceToClose);
    contentPane.getChildren().remove(interfaceToClose);
  }

  public CodingInterface getActiveInterface(){
    return activeInterface;
  }

  public void setActiveInterface(CodingInterface activeInterface){
    this.activeInterface = activeInterface;
  }

  public List<CodingInterface> getInterfaces(){
    return interfaces;
  }

  public void prepareDrag() {
    getChildren().add(dragOverPane);
  }

  public void endDrag() {
    getChildren().remove(dragOverPane);
  }
}
