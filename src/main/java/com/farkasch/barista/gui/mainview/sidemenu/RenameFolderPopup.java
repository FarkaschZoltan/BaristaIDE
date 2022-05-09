package com.farkasch.barista.gui.mainview.sidemenu;


import com.farkasch.barista.gui.component.FolderDropdown.FolderDropdownItem;
import java.io.File;
import org.springframework.stereotype.Component;

@Component
public class RenameFolderPopup extends AbstractRenamePopup {

  @Override
  protected void save() {
    String newFolderName = newNameField.getText();
    if (!persistenceService.getOpenProject().getFolders().contains(itemToRename.getParentPath() + "\\" + newFolderName)){
      fileService.renameFolder(new File(itemToRename.getPath()), newFolderName, itemToRename);
      close();
    } else {
      warningPopup.showWindow("Error", "A folder with this name already exists!", null);
    }
  }

  @Override
  protected void onLoad(FolderDropdownItem folderDropdownItem) {
    setTitle("Rename Folder");
    newNameField.setText("");
    newNameLabel.setText("Folder Name: ");
    this.itemToRename = folderDropdownItem;
  }
}
