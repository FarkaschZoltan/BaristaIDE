package com.farkasch.barista.util;

import org.springframework.stereotype.Component;

//A custom drag board to more easily implement a drag-and-drop feature, with the use of springs dependency injection.
@Component
public class BaristaDragBoard {
  private Object draggedItem;
  private Object dragTarget;

  public BaristaDragBoard(){

    draggedItem = null;
    dragTarget = null;
  }

  public BaristaDragBoard(Object draggedItem){
    this.draggedItem = draggedItem;
  }

  public Object getDraggedItem() {
    return draggedItem;
  }

  public void setDraggedItem(Object draggedItem) {
    this.draggedItem = draggedItem;
  }

  public Object getDragTarget() {
    return dragTarget;
  }

  public void setDragTarget(Object dragTarget) {
    this.dragTarget = dragTarget;
  }

  public void dragDone(){
    this.draggedItem = null;
    this.dragTarget = null;
  }
}
