package com.farkasch.barista.gui.mainview.sidemenu;

import com.farkasch.barista.gui.component.FolderDropdown.FolderDropdownItem;
import org.springframework.stereotype.Component;

@Component
public class RenameProjectPopup extends AbstractRenamePopup {

  @Override
  protected void save() {
    String newProjectName = newNameField.getText();
    if(fileService.getProjects().stream().filter(baristaProject -> baristaProject.getProjectName().equals(newProjectName)).toList().size() == 0){
      fileService.renameProject(newProjectName, itemToRename);
      close();
    } else {
      warningPopup.showWindow("Error", "A project with this name already exists!", null);
    }
  }

  @Override
  protected void onLoad(FolderDropdownItem folderDropdownItem) {
    setTitle("Rename Project");
    newNameField.setText("");
    newNameLabel.setText("Project Name: ");
    this.itemToRename = folderDropdownItem;
  }
}