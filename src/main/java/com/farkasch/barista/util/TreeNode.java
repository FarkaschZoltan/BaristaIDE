package com.farkasch.barista.util;

import java.util.ArrayList;
import java.util.function.Consumer;

public class TreeNode<V> implements Cloneable {

  private TreeNode parent;
  private ArrayList<TreeNode> children;
  private V value;

  public TreeNode() {
    parent = null;
    children = new ArrayList<>();
    value = null;
  }

  public TreeNode(TreeNode parent, ArrayList<TreeNode> children, V value) {
    this.parent = parent;
    this.children = children;
    this.value = value;
  }

  public TreeNode getParent() {
    return parent;
  }

  public void setParent(TreeNode parent) {
    this.parent = parent;
    parent.addChild(this);
  }

  public ArrayList<TreeNode> getChildren() {
    return children;
  }

  public void addChild(TreeNode child) {
    children.add(child);
  }

  public void addChild(TreeNode child, int index) {
    children.add(index, child);
  }

  public V getValue() {
    return value;
  }

  public void setValue(V value) {
    this.value = value;
  }

  public int getHeight() {
    return getHeight(0);
  }

  public int getHeight(int height) {
    int currentHeight = height;
    for (TreeNode child : children) {
      int childHeight = child.getHeight(height + 1);
      if (childHeight > currentHeight) {
        currentHeight = childHeight;
      }
    }
    return currentHeight;
  }

  public void cutBelow() {
    children.clear();
  }

  public void doActionPreorder(Consumer<V> action) {
    action.accept(value);
    children.forEach(child -> child.doActionPreorder(action));
  }

  public void doActionPostorder(Consumer<V> action) {
    children.forEach(child -> child.doActionPostorder(action));
    action.accept(value);
  }

  public boolean removeNode(TreeNode node){
    if(children.contains(node)){
      return children.remove(node);
    } else {
      children.forEach(child -> child.removeNode(node));
    }
    return false;
  }
}
