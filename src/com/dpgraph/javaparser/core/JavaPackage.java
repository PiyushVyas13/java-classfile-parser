package com.dpgraph.javaparser.core;

import java.util.HashSet;

public class JavaPackage {
    private final String name;
    private final HashSet<JavaClass> classes;


    public JavaPackage(String name) {
        this.name = name;
        classes = new HashSet<>();
    }

    public String getName() {
        return name;
    }

    public void addClass(JavaClass javaClass) {
        classes.add(javaClass);
    }

    public HashSet<JavaClass> getClasses() {
        return classes;
    }

    public int getClassCount() {
        return classes.size();
    }

    public int getAbstractClassCount() {
        int count = 0;

        for(JavaClass javaClass : classes) {
            if(javaClass.isAbstract()) {
                count++;
            }
        }

        return count;
    }

    public int getConcreteClassCount() {
        int count = 0;

        for(JavaClass javaClass : classes) {
            if(!javaClass.isAbstract()) {
                count++;
            }
        }

        return count;
    }

    @Override
    public String toString() {
        return "com.dpgraph.parser.core.JavaPackage{" +
                "name='" + name + '\'' +
                ", classes=" + classes +
                '}';
    }
}
