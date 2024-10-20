package com.dpgraph.javaparser.sample;

import com.dpgraph.javaparser.util.PackageFilter;

import java.util.List;

public class Animal<T> extends PackageFilter implements Cloneable{
    private String name;
    private String type;
    private List<String> features;
    private T something;
    public static final int hello = 5;

    public Animal(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public Animal() {
        name = "default";
        type = "default";
    }

    public T getTypeParameter() {
        return something;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getFeatures() {
        return features;
    }

    @Override
    public Animal clone() {
        try {
            // TODO: copy mutable state here, so the clone can't change the internals of the original
            return (Animal) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    private static class SomeClass {
        public final int i;
        public static final String HELLO = "gello";

        public int getI() {
            return i;
        }

        public SomeClass() {
            i = 1;
        }

        public SomeClass(int i) {
            this.i = i;
        }
    }
}
