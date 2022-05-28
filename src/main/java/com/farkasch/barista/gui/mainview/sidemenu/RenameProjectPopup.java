package com.farkasch.barista.gui.mainview.sidemenu;

import com.farkasch.barista.gui.component.FolderDropdown.FolderDropdownItem;
import org.springframework.stereotype.Component;

@Component
public class RenameProjectPopup extends AbstractProjectPopup {

  @Override
  protected void save() {
    String newProjectName = itemTextField.getText();
    if(!newProjectName.equals("")){
      if(fileService.getProjects().stream().filter(baristaProject -> baristaProject.getProjectName().equals(newProjectName)).toList().size() == 0){
        fileService.renameProject(newProjectName, item);
        close();
      } else {
        warningPopup.showWindow("Error", "A project with this name already exists!", null);
      }
    } else {
      warningPopup.showWindow("Error", "Project name field must not be empty!", null);
    }
  }

  @Override
  protected void onLoad(FolderDropdownItem folderDropdownItem) {
    setTitle("Rename Project");
    itemTextField.setText("");
    itemTextFieldLabel.setText("Project Name: ");
    this.item = folderDropdownItem;
  }
}