package com.dpgraph.javaparser.util;

import com.dpgraph.javaparser.core.JavaPackage;

import java.io.*;
import java.util.*;

public class PropertyConfigurator {
    private final Properties properties;
    public static final String PROPERTY_FILE_NAME = "parser.properties";

    public PropertyConfigurator() {
        String userHome = System.getProperty("user.home");
        File propertyFile = new File(userHome, PROPERTY_FILE_NAME);


        Properties properties = new Properties();

        InputStream inputStream = null;

        try {
            inputStream = new FileInputStream(propertyFile);
        } catch (Exception e) {
           inputStream = PropertyConfigurator.class.getResourceAsStream("/" + PROPERTY_FILE_NAME);
        }

        try {
            if(inputStream != null) {
                properties.load(inputStream);
            }
        } catch (IOException ignored) {}
        finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException ignored) {}
        }

        this.properties = properties;
    }

    public List<String> getFilteredPackages() {
        List<String> packages = new ArrayList<>();

        for(String key : properties.stringPropertyNames()) {
            if(key.startsWith("ignore")) {
                String path = properties.getProperty(key);
                StringTokenizer tokenizer = new StringTokenizer(path, ",");

                while(tokenizer.hasMoreTokens()) {
                    String name = tokenizer.nextToken().trim();
                    packages.add(name);
                }
            }
        }

        return packages;
    }

    public List<JavaPackage> getConfiguredPackages() {
        List<JavaPackage> packages = new ArrayList<>();

        Enumeration<?> enumeration = properties.propertyNames();

        while (enumeration.hasMoreElements()) {
            String key = (String) enumeration.nextElement();

            if(!key.startsWith("ignore")) {
                String path = properties.getProperty(key);
                packages.add(new JavaPackage(path));
            }
        }

        return packages;
    }
}
