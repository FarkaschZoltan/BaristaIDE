package com.farkasch.barista.gui.mainview.sidemenu;

import com.farkasch.barista.gui.component.FolderDropdown.FolderDropdownItem;
import com.farkasch.barista.util.Result;
import com.farkasch.barista.util.enums.ResultTypeEnum;
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
    Result fileRenamed = fileService.renameFile(new File(item.getPath()), newFileName, item);
    if(fileRenamed.getResult().equals(ResultTypeEnum.OK)){
      close();
    } else {
      warningPopup.showWindow(fileRenamed);
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
