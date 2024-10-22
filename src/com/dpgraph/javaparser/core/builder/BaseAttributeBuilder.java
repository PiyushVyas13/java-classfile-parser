package com.dpgraph.javaparser.core.builder;

import com.dpgraph.javaparser.parser.AttributeInfo;
import com.dpgraph.javaparser.parser.Constant;
import com.dpgraph.javaparser.parser.FieldOrMethodInfo;
import com.dpgraph.javaparser.util.Modifier;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseAttributeBuilder<T> implements AttributeBuilder<T> {
    protected static final int ACC_FINAL = 0x0010;
    protected static final int ACC_ABSTRACT = 0x0400;
    protected static final int ACC_STATIC = 0x0008;

    protected static final int ACC_PUBLIC = 0x0001;
    protected static final int ACC_PRIVATE = 0x0002;
    protected static final int ACC_PROTECTED = 0x0004;

    protected final Constant[] constantPool;

    public BaseAttributeBuilder(Constant[] pool) {
        constantPool = pool;
    }

    protected String getName(FieldOrMethodInfo poolEntry) {
        return (String) constantPool[poolEntry.getNameIndex()].getValue();
    }

    protected boolean parseFlag(FieldOrMethodInfo poolEntry, int constant) {
        int flags = poolEntry.getAccessFlags();
        return ((flags & constant) != 0);
    }

    protected String parseDescriptor(FieldOrMethodInfo poolEntry) {
        if(poolEntry.getSignature() != null) {

            byte[] signatureValue = poolEntry.getSignature().getValue();

            int b0 = signatureValue[0] < 0 ? signatureValue[0]+256 : signatureValue[0];
            int b1 = signatureValue[1] < 0 ? signatureValue[1]+256 : signatureValue[1];

            int poolEntryIndex = b0 * 256 + b1;


            return (String) constantPool[poolEntryIndex].getValue();
        } else {
            return (String) constantPool[poolEntry.getDescriptorIndex()].getValue();
        }
    }

    protected Modifier parseModifier(FieldOrMethodInfo poolEntry) {
        if(parseFlag(poolEntry, ACC_PUBLIC)) {
            return Modifier.PUBLIC;
        }

        if(parseFlag(poolEntry, ACC_PRIVATE)) {
            return Modifier.PRIVATE;
        }

        if(parseFlag(poolEntry, ACC_PROTECTED)) {
            return Modifier.PROTECTED;
        }

        return Modifier.DEFAULT;
    }

    protected String parseType(String type) {
        return switch (type.charAt(0)) {
            case 'B' -> "byte";
            case 'C' -> "char";
            case 'D' -> "double";
            case 'F' -> "float";
            case 'I' -> "int";
            case 'J' -> "long";
            case 'S' -> "short";
            case 'Z' -> "boolean";
            case 'V' -> "void";
            case 'T' -> type.substring(1).replace(';', ' ').trim();
            case '[' -> parseType(type.substring(1)) + "[]";
            case 'L' -> parseObjectType(type);
            default -> throw new IllegalArgumentException("Unknown type: " + type);
        };
    }

    private String parseObjectType(String type) {
        StringBuilder result = new StringBuilder();
        int depth = 0;
        boolean inGeneric = false;
        List<String> genericTypes = new ArrayList<>();

        for (int i = 1; i < type.length(); i++) {
            char c = type.charAt(i);
            if (c == '<') {
                inGeneric = true;
                depth++;
            } else if (c == '>') {
                depth--;
                if (depth == 0) {
                    result.append('<').append(String.join(", ", genericTypes)).append('>');
                    inGeneric = false;
                }
            } else if (c == ';' && depth == 0) {
                break;
            } else if (inGeneric && c == 'L') {
                StringBuilder nestedType = new StringBuilder();
                while (i < type.length() && type.charAt(i) != ';' && type.charAt(i) != '>') {
                    nestedType.append(type.charAt(i++));
                }
                genericTypes.add(parseType(nestedType.toString()));
                i--;
            } else if (!inGeneric) {
                result.append(c);
            }
        }
        return result.toString().replace('/', '.');
    }

    private int findGenericEnd(String descriptor, int start) {
        int depth = 1;
        for (int i = start; i < descriptor.length(); i++) {
            if (descriptor.charAt(i) == '<') {
                depth++;
            } else if (descriptor.charAt(i) == '>') {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        throw new IllegalArgumentException("Unmatched generic type brackets");
    }


}
