package com.dpgraph.javaparser.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class JavaPackage implements JavaElement{
    private final String name;
    private final List<JavaElement> elements;



    public JavaPackage(String name) {
        this.name = name;
        elements = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    @Override
    public void addElement(JavaElement element) {
        if(elements.contains(element)) {
            return;
        }
        elements.add(element);
    }

    @Override
    public List<JavaElement> getElements() {
        return elements;
    }

    @Override
    public boolean isClass() {
        return false;
    }

    @Override
    public boolean isPackage() {
        return true;
    }

    public int getClassCount() {
        int count = 0;
        for(JavaElement element : elements) {
            if(element.isClass()) {
                count++;
            }
        }
        return count;
    }

    public int getAbstractClassCount() {
        int count = 0;

        for(JavaElement element : elements) {
            if(element.isClass() && ((JavaClass) element).isAbstract()) {
                count++;
            }
        }

        return count;
    }

    public int getConcreteClassCount() {
        int count = 0;

        for(JavaElement element : elements) {
            if(element.isClass() && !((JavaClass) element).isAbstract()) {
                count++;
            }
        }

        return count;
    }

    @Override
    public String toString() {
        return "com.dpgraph.parser.core.JavaPackage{" +
                "name='" + name + '\'' +
                ", elements=" + elements +
                '}';
    }
}
