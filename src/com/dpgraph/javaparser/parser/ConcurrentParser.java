package com.dpgraph.javaparser.parser;

import com.dpgraph.javaparser.core.Field;
import com.dpgraph.javaparser.core.JavaClass;
import com.dpgraph.javaparser.core.JavaPackage;
import com.dpgraph.javaparser.core.Method;
import com.dpgraph.javaparser.util.FileManager;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

public class ConcurrentParser {
    private final ExecutorService executorService;
    private final ConcurrentHashMap<String, JavaClass> repository;
    private final FileManager fileManager;

    public ConcurrentParser(int numThreads) {
        executorService = Executors.newFixedThreadPool(numThreads);
        repository = new ConcurrentHashMap<>();
        this.fileManager = new FileManager();
    }

    public ConcurrentHashMap<String, JavaClass> parseClassFiles(File classFileDirectory) throws InterruptedException, IOException {
        if(!classFileDirectory.isDirectory()) {
            throw new IllegalArgumentException("The given file is not a directory");
        }

        List<Future<?>> futures = new ArrayList<>();

        fileManager.addDirectory(classFileDirectory);

        Collection<File> files = fileManager.extractFiles();
        for(final File file : files) {
            if(fileManager.acceptFile(file)) {
                futures.add(executorService.submit(() -> {
                    try(InputStream stream = new BufferedInputStream(new FileInputStream(file))) {
                        Parser newParser = new ClassFileParser();
                        JavaClass javaClass = newParser.parse(stream);
                        String className = javaClass.getName();


                        repository.compute(className, (key, existingValue) -> {
                            if(existingValue instanceof PlaceholderJavaClass || existingValue == null) {
                                return javaClass;
                            }
                            return existingValue;
                        });

                        for(String outGoingDependency : javaClass.getOutGoingDependencies()) {
                            repository.computeIfAbsent(outGoingDependency, k -> new PlaceholderJavaClass(outGoingDependency));

                            repository.computeIfPresent(outGoingDependency, (key, value) -> {
                                if(value instanceof PlaceholderJavaClass) {
                                    return value;
                                }

                                value.getIncomingDependencies().add(className);
                                return value;
                            });
                        }

                    } catch (IOException fnfe) {
                        fnfe.printStackTrace();
                    }
                }));
            }
        }

        for(Future<?> future : futures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

        return getRepository();
    }


    public ConcurrentHashMap<String, JavaClass> getRepository() {
        return repository;
    }

    public static class PlaceholderJavaClass extends JavaClass {
        public PlaceholderJavaClass(String name) {
            super(name);
        }

        @Override
        public List<Field> getFields() {
            throw new UnsupportedOperationException("Not supported for placeholder.");
        }

        @Override
        public List<JavaPackage> getImportedPackages() {
            throw new UnsupportedOperationException("Not supported for placeholder.");
        }

        @Override
        public List<Method> getMethods() {
            throw new UnsupportedOperationException("Not supported for placeholder.");
        }

        @Override
        public Set<String> getIncomingDependencies() {
            throw new UnsupportedOperationException("Not supported for placeholder.");
        }

        @Override
        public Set<String> getOutGoingDependencies() {
            throw new UnsupportedOperationException("Not supported for placeholder.");
        }

        @Override
        public String getPackageName() {
            throw new UnsupportedOperationException("Not supported for placeholder.");
        }

        @Override
        public String getSourceFile() {
            throw new UnsupportedOperationException("Not supported for placeholder.");
        }

        @Override
        public String getSignature() {
            throw new UnsupportedOperationException("Not supported for placeholder.");
        }
    }
}
