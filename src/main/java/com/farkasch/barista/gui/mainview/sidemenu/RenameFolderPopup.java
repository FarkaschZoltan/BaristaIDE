package com.farkasch.barista.gui.mainview.sidemenu;


import com.farkasch.barista.gui.component.FolderDropdown.FolderDropdownItem;
import java.io.File;
import org.springframework.stereotype.Component;

@Component
public class RenameFolderPopup extends AbstractProjectPopup {

  @Override
  protected void save() {
    String newFolderName = itemTextField.getText();
    if (!persistenceService.getOpenProject().getFolders().contains(item.getParentPath() + "\\" + newFolderName)){
      fileService.renameFolder(new File(item.getPath()), newFolderName, item);
      close();
    } else {
      warningPopup.showWindow("Error", "A folder with this name already exists!", null);
    }
  }

  @Override
  protected void onLoad(FolderDropdownItem folderDropdownItem) {
    setTitle("Rename Folder");
    itemTextField.setText("");
    itemTextFieldLabel.setText("Folder Name: ");
    this.item = folderDropdownItem;
  }
}
