package com.dpgraph.javaparser.parser;

import com.dpgraph.javaparser.core.JavaClass;
import com.dpgraph.javaparser.util.FileManager;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ClassBuilder {
    private final Parser parser;
    private final FileManager fileManager;

    public ClassBuilder(Parser parser, FileManager fileManager) {
        this.parser = parser;
        this.fileManager = fileManager;
    }

    public ClassBuilder() {
        this(new ClassFileParser(), new FileManager());
    }

    public List<JavaClass> build() {
        List<JavaClass> files = new ArrayList<>();

        try {
            for(File file : fileManager.extractFiles()) {
                files.addAll(buildClasses(file));
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        return files;
    }

    public List<JavaClass> buildClasses(File file) throws IOException {
        if(fileManager.acceptFile(file)) {

            try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
                JavaClass parsedClass = parser.parse(is);
                List<JavaClass> classes = new ArrayList<>();

                classes.add(parsedClass);
                return classes;
            }
        }

        throw new FileNotFoundException(file.getAbsolutePath());
    }

    public ConcurrentHashMap<String, JavaClass> buildGraph(File directory) throws InterruptedException, IOException {
        int numThreads = Runtime.getRuntime().availableProcessors();
        ConcurrentParser concurrentParser = new ConcurrentParser(numThreads);
        return concurrentParser.parseClassFiles(directory);
    }

    public int countClasses() {
        Parser abstractParser = new Parser() {
            @Override
            public JavaClass parse(InputStream inputStream) throws IOException {
                return new JavaClass("");
            }
        };

        ClassBuilder builder = new ClassBuilder(abstractParser, fileManager);
        List<JavaClass> classes = builder.build();

        return classes.size();
    }
}
