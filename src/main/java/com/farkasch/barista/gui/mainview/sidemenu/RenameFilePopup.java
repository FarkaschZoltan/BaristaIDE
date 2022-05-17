package com.farkasch.barista.gui.mainview.sidemenu;

import com.farkasch.barista.gui.component.FolderDropdown.FolderDropdownItem;
import com.google.common.io.Files;
import java.io.File;
import org.springframework.stereotype.Component;

@Component
public class RenameFilePopup extends AbstractProjectPopup {

  @Override
  protected void save() {
    String newFileName = itemTextField.getText();
    if (newFileName.split("\\.").length < 2) {
      newFileName = newFileName.concat("." + Files.getFileExtension(item.getPath()));
    }
    if (!persistenceService.getOpenProject().getSourceFiles().contains(item.getParentPath() + "\\" + newFileName)){
      fileService.renameFile(new File(item.getPath()), newFileName, item);
      close();
    } else {
      warningPopup.showWindow("Error", "A file with this name already exists!", null);
    }
  }

  @Override
  protected void onLoad(FolderDropdownItem folderDropdownItem) {
    setTitle("Rename File");
    itemTextField.setText("");
    itemTextFieldLabel.setText("File Name: ");
    this.item = folderDropdownItem;
  }
}
