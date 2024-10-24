package com.dpgraph.javaparser;

import com.dpgraph.javaparser.core.JavaClass;
import com.dpgraph.javaparser.core.JavaElement;
import com.dpgraph.javaparser.core.JavaPackage;
import com.dpgraph.javaparser.parser.ClassBuilder;
import com.dpgraph.javaparser.parser.ClassFileParser;
import com.dpgraph.javaparser.parser.ConcurrentParser;
import com.dpgraph.javaparser.parser.Parser;
import com.dpgraph.javaparser.util.FileManager;
import com.dpgraph.javaparser.util.PropertyConfigurator;
import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Driver {
    private static final FileManager fileManager = new FileManager();
    private static final Parser parser = new ClassFileParser();
    private static final ClassBuilder classBuilder = new ClassBuilder(parser, fileManager);


    public static List<JavaPackage> transformClasses(final List<JavaClass> classes) {
        Map<String, JavaPackage> packageMap = new HashMap<>();

        for(JavaClass javaClass : classes) {
            if (!(javaClass instanceof ConcurrentParser.PlaceholderJavaClass)) {


                String packageName = javaClass.getPackageName();

                if (!packageMap.containsKey(packageName)) {
                    packageMap.put(packageName, new JavaPackage(packageName));
                }

                packageMap.get(packageName).addElement(javaClass);
            } else {
                System.out.println(javaClass);
            }
        }

        return new ArrayList<>(packageMap.values());
    }

    public static JavaElement buildHierarchy(final List<JavaClass> classes) {
        Map<String, JavaPackage> packageMap = new HashMap<>();
        JavaPackage root = new JavaPackage("root");
        packageMap.put("", root);

        for(JavaClass javaClass : classes) {
            if (!(javaClass instanceof ConcurrentParser.PlaceholderJavaClass)) {
                String[] packageParts = javaClass.getPackageName().split("\\.");
                String currentPath = "";
                JavaPackage currentPackage = root;

                for(String part : packageParts) {
                    String newPath = currentPath.isEmpty() ? part : "." + part;
                    JavaPackage newPackage = packageMap.get(newPath);

                    if(newPackage == null) {
                        newPackage = new JavaPackage(newPath);
                        packageMap.put(newPackage.getName(), newPackage);
                        currentPackage.addElement(newPackage);
                    }

                    currentPackage = newPackage;
                    currentPath = newPath;
                }

                currentPackage.addElement(javaClass);

            } else {
                System.out.println(javaClass);
            }

        }

        if(root.getElements().size() == 1 && root.getElements().get(0).isPackage()) {
            return root.getElements().get(0);
        }

        return root;
    }

    public static void printHierarchy(JavaElement element, String indent) {
        System.out.println(indent + element.getName() +
                (element.isPackage() ? " (Package)" : " (Class)"));

        if(element.isPackage()) {
            for (JavaElement child : element.getElements()) {
                printHierarchy(child, indent + "  ");
            }
        }

    }

    public static void analyzeAsync(File classFileDirectory) throws InterruptedException, IOException {
        long startTime = System.currentTimeMillis();
        System.out.println("Parsing files...");
        ConcurrentHashMap<String, JavaClass> repo = classBuilder.buildGraph(classFileDirectory);

//        List<JavaPackage> packages = transformClasses(new ArrayList<>(repo.values()));
//
//        for(JavaPackage javaPackage : packages) {
//            System.out.println(javaPackage);
//        }

        JavaElement element = buildHierarchy(new ArrayList<>(repo.values()));
        printHierarchy(element, "");

        writeToJson(element);

        long endTime = System.currentTimeMillis();

        double time = (double) (endTime - startTime) / 1000;
        System.out.println("Parsing finished in " + time + " seconds.");

    }

    public static void writeToJson(Object packages) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File("dependencies.json"), packages);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        // Check if at least one argument is provided (path to class file directory)
        if (args.length < 1) {
            System.err.println("Error: Path to the class file directory is required.");
            System.err.println("Usage: java DependencyGraphCLI <classFileDirectory> [outputPath]");
            System.exit(1);
        }

        // Parse the arguments
        String classFileDirectory = args[0];
        String outputPath = args.length > 1 ? args[1] : "dependencies.json"; // Default output file is packages.json

        // Validate the class file directory
        File directory = new File(classFileDirectory);
        if (!directory.exists() || !directory.isDirectory()) {
            System.err.println("Error: The specified class file directory does not exist or is not a directory.");
            System.exit(1);
        }

        try {
            analyzeAsync(directory);
            // analyzeAsync(new File("."));
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
