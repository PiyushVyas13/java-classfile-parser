package com.dpgraph.javaparser.util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

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
}
