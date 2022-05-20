package com.farkasch.barista.gui.mainview.sidemenu;

import com.farkasch.barista.gui.component.FolderDropdown.FolderDropdownItem;
import com.farkasch.barista.gui.component.WarningPopup;
import com.farkasch.barista.services.FileService;
import com.farkasch.barista.services.PersistenceService;
import com.farkasch.barista.util.Result;
import com.farkasch.barista.util.enums.ResultTypeEnum;
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
    Result folderCreated = fileService.createFolder(item.getPath() + "\\" + itemTextField.getText(), item);
    if(folderCreated.getResult().equals(ResultTypeEnum.OK)){
      close();
    } else {
      warningPopup.showWindow(folderCreated);
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
