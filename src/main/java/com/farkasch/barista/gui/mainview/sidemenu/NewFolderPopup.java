package com.farkasch.barista.gui.mainview.sidemenu;

import com.farkasch.barista.gui.component.FolderDropdown.FolderDropdownItem;
import com.farkasch.barista.gui.component.WarningPopup;
import com.farkasch.barista.services.FileService;
import com.farkasch.barista.services.PersistenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NewFolderPopup extends AbstractProjectPopup {

  @Autowired
  private FileService fileService;
  @Autowired
  private PersistenceService persistenceService;
  @Autowired
  private WarningPopup warningPopup;

  @Override
  protected void save(){
    if (!persistenceService.getOpenProject().getFolders().contains(item.getPath() + "\\" + itemTextField.getText())) {
      fileService.createFolder(item.getPath() + "\\" + itemTextField.getText(), item);
      close();
    } else {
      warningPopup.showWindow("Error", "A folder with this name already exists!", null);
    }
  }

  @Override
  protected void onLoad(FolderDropdownItem creationFolder){
    setTitle("New Folder");
    applyButton.setText("Create");
    itemTextField.setText("");
    itemTextFieldLabel.setText("Folder name: ");
    this.item = creationFolder;
  }
}
