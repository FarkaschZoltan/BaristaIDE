package com.farkasch.barista.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;

public class FileService {

    public static void saveFile(File file, String content) {
        try {
            FileOutputStream fos = new FileOutputStream(file, false);
            fos.write(content.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static File createFile(String path) throws FileAlreadyExistsException {
        File newFile = new File(path);
        try {
            newFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return newFile;
    }
}
