package com.dpgraph.javaparser.util;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class FileManager {
    private final ArrayList<File> directories;

    public FileManager() {
        directories = new ArrayList<>();
    }

    public void addDirectory(File file) throws IOException{
        if(file.isDirectory()) {
            directories.add(file);
        } else {
            throw new IOException("The given file is not a directory: " + file.getName());
        }
    }

    public boolean acceptFile(File file) {
        if (!file.isFile()) {
            return false;
        }

        return file.getName().toLowerCase(Locale.ROOT).endsWith(".class");
    }

    private void addFile(File file, Collection<File> collection) {
        if(!collection.contains(file)) {
            collection.add(file);
        }
    }

    public Collection<File> extractFiles() {
        Collection<File> files = new TreeSet<>();

        for (File directory : directories) {
            collectFiles(directory, files);
        }

        return files;
    }

    private void collectFiles(File directory, Collection<File> collection) {
        if(directory.isFile()) {
            addFile(directory, collection);
            return;
        }

        String[] directoryFiles = directory.list();

        for(String fileName : directoryFiles) {
            File file = new File(directory, fileName);

            if(acceptFile(file)) {
                addFile(file, collection);
            } else if (file.isDirectory()) {
                collectFiles(file, collection);
            }
        }
    }
}
