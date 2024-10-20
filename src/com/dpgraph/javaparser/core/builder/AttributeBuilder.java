package com.dpgraph.javaparser.core.builder;

import com.dpgraph.javaparser.parser.FieldOrMethodInfo;

public interface AttributeBuilder<T> {
    T parse(FieldOrMethodInfo poolEntry);
}
