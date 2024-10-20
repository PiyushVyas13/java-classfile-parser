package com.dpgraph.javaparser.core;

import com.dpgraph.javaparser.util.Modifier;

import java.util.List;

public record Method (
     Modifier accessModifier,
     boolean isStatic,
     boolean isFinal,
     boolean isAbstract,
     String name,
     List<String> parameters,
     String returnType
) {
    public Method() {
        this(null, false, false, false, null, null, null);
    }
}
