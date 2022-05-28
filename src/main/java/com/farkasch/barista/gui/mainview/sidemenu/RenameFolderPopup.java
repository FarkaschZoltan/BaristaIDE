package com.farkasch.barista.gui.mainview.sidemenu;


import com.farkasch.barista.gui.component.FolderDropdown.FolderDropdownItem;
import com.farkasch.barista.util.Result;
import com.farkasch.barista.util.enums.ResultTypeEnum;
import java.io.File;
import org.springframework.stereotype.Component;

@Component
public class RenameFolderPopup extends AbstractProjectPopup {

  @Override
  protected void save() {
    String newFolderName = itemTextField.getText();
    if(!newFolderName.equals("")){
      Result folderRenamed = fileService.renameFolder(new File(item.getPath()), newFolderName, item);
      if(folderRenamed.getResult().equals(ResultTypeEnum.OK)){
        close();
      } else {
        warningPopup.showWindow(folderRenamed);
      }
    } else {
      warningPopup.showWindow("Error", "Folder name field must not be empty!", null);
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
