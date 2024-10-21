package com.dpgraph.javaparser;

import com.dpgraph.javaparser.core.JavaClass;
import com.dpgraph.javaparser.core.JavaPackage;
import com.dpgraph.javaparser.parser.ClassBuilder;
import com.dpgraph.javaparser.parser.ClassFileParser;
import com.dpgraph.javaparser.parser.Parser;
import com.dpgraph.javaparser.util.FileManager;
import com.dpgraph.javaparser.util.PropertyConfigurator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Driver {
    private static final HashMap<String, JavaPackage> packages = new HashMap<>();
    private static final FileManager fileManager = new FileManager();
    private static final Parser parser = new ClassFileParser();
    private static final ClassBuilder classBuilder = new ClassBuilder(parser, fileManager);
//    private List<JavaClass> components;

    public static void addPackages(List<JavaPackage> pkg) {
        for(JavaPackage javaPackage : pkg) {
            if(!packages.containsValue(javaPackage)) {
                packages.put(javaPackage.getName(), javaPackage);
            }
        }
    }

    public static void addDirectory(File directory) throws IOException {
        fileManager.addDirectory(directory);
    }

    public static void analyze() {
        List<JavaClass> classes = classBuilder.build();

        for(JavaClass javaClass : classes) {
            System.out.println(javaClass);
        }
    }

    public static void main(String[] args) {


        try {
            addDirectory(new File("."));
            analyze();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
