package com.dpgraph.javaparser.core;

import com.dpgraph.javaparser.util.Modifier;

public record Field (
        String name,
        String type,
        boolean isStatic,
        boolean isFinal,
        Modifier modifier
) {}
