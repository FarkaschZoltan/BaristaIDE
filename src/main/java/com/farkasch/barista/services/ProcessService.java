package com.farkasch.barista.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.util.Pair;
import org.springframework.lang.Nullable;

public class ProcessService {

    public static List<String> getDirs(@Nullable String folder) {
        List<String> dirs = new ArrayList<>();

        ProcessBuilder pb = new ProcessBuilder();
        if (folder == null) {
            pb.directory(new File("C:\\Users"));
        } else {
            pb.directory(new File("C:\\Users" + folder));
        }
        pb.command("CMD", "/C", "dir");

        try {
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
            String line;

            while ((line = reader.readLine()) != null) {
                //System.out.println(line);
                List<String> splitLine = Arrays.asList(line.split(" "));
                if (splitLine.contains("<DIR>") && !(splitLine.contains(".") || splitLine.contains(
                    ".."))) {
                    dirs.add(splitLine.get(splitLine.size() - 1));
                }
            }

            //dirs.stream().forEach(s -> System.out.println("-> " + s));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return dirs;
    }

    public static List<Pair<String, Boolean>> getDirsAndFiles(@Nullable String folder){
        List<Pair<String, Boolean>> dirsAndFiles = new ArrayList<>();

        ProcessBuilder pb = new ProcessBuilder();
        if (folder == null) {
            pb.directory(new File("C:\\Users"));
        } else {
            pb.directory(new File("C:\\Users" + folder));
        }
        pb.command("CMD", "/C", "dir");

        try {
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
            String line;

            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                List<String> splitLine = Arrays.asList(line.split(" "));
                if (splitLine.contains("<DIR>") && !(splitLine.contains(".") || splitLine.contains(
                    ".."))) {
                    dirsAndFiles.add(new Pair<String, Boolean>(splitLine.get(splitLine.size() - 1), false));
                }
                else if(splitLine.get(splitLine.size()-1).matches(".*[.].[a-z0-9]{2,3}")){
                    dirsAndFiles.add(new Pair<String, Boolean>(splitLine.get(splitLine.size() - 1), true));
                }
            }

            dirsAndFiles.stream().forEach(s -> System.out.println("-> " + s));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return dirsAndFiles;
    }

}
