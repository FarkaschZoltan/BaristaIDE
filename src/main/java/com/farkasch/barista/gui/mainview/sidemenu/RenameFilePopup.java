package com.farkasch.barista.gui.mainview.sidemenu;

import com.farkasch.barista.gui.component.FolderDropdown.FolderDropdownItem;
import com.google.common.io.Files;
import java.io.File;
import org.springframework.stereotype.Component;

@Component
public class RenameFilePopup extends AbstractRenamePopup {

  @Override
  protected void save() {
    String newFileName = newNameField.getText();
    if (newFileName.split("\\.").length < 2) {
      newFileName = newFileName.concat("." + Files.getFileExtension(itemToRename.getPath()));
    }
    if (!persistenceService.getOpenProject().getSourceFiles().contains(itemToRename.getParentPath() + "\\" + newFileName)){
      fileService.renameFile(new File(itemToRename.getPath()), newFileName, itemToRename);
      close();
    } else {
      warningPopup.showWindow("Error", "A file with this name already exists!", null);
    }
  }

  @Override
  protected void onLoad(FolderDropdownItem folderDropdownItem) {
    setTitle("Rename File");
    newNameField.setText("");
    newNameLabel.setText("File Name: ");
    this.itemToRename = folderDropdownItem;
  }
}
