package com.dpgraph.javaparser.core;

import java.util.List;
import java.util.Set;

public interface JavaElement {
    String getName();
    void addElement(JavaElement element);
    List<JavaElement> getElements();
    boolean isClass();
    boolean isPackage();
}
