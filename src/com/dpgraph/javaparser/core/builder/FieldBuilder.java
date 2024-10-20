package com.dpgraph.javaparser.core.builder;

import com.dpgraph.javaparser.core.Field;
import com.dpgraph.javaparser.parser.Constant;
import com.dpgraph.javaparser.parser.FieldOrMethodInfo;
import com.dpgraph.javaparser.util.Modifier;

public class FieldBuilder extends BaseAttributeBuilder<Field> {
    public FieldBuilder(Constant[] pool) {
        super(pool);
    }

    @Override
    public Field parse(FieldOrMethodInfo poolEntry) {
        String name = getName(poolEntry);
        String descriptor = parseDescriptor(poolEntry);
        Modifier modifier = parseModifier(poolEntry);

        String type = parseType(descriptor);
        boolean isStatic = parseFlag(poolEntry, ACC_STATIC);
        boolean isFinal = parseFlag(poolEntry, ACC_FINAL);


        return new Field(name, type, isStatic, isFinal, modifier);
    }
}
