package com.farkasch.barista.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class SaveService {
    public static void saveFile(File file, String content){
        try{
            FileOutputStream fos = new FileOutputStream(file, false);
            fos.write(content.getBytes());
            fos.close();
        } catch(FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
