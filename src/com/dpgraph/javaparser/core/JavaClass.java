package com.dpgraph.javaparser.core;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;


public class JavaClass {
    private String className;
    private String packageName;
    private boolean isAbstract;
    private boolean isFinal;
    private boolean isInterface;
    private final HashMap<String, JavaPackage> imports;
    private String sourceFile;
    private String signature;
    private final List<Field> fields;
    private final List<Method> methods;



    public JavaClass(String name) {
        className = name;
        packageName = "default";
        isAbstract = false;
        imports = new HashMap<>();
        sourceFile = "Unknown";
        signature = "default";
        fields = new ArrayList<>();
        methods = new ArrayList<>();
        isFinal = false;
        isInterface = false;
    }

    public void setName(String className) {
        this.className = className;
    }

    public String getName() {
        return className;
    }

    public void addField(Field field) {
        this.fields.add(field);
    }

    public void addMethod(Method method) {
        this.methods.add(method);
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void addImportedPackage(JavaPackage javaPackage) {
        if(!javaPackage.getName().equals(getPackageName())) {
            imports.put(javaPackage.getName(), javaPackage);
        }
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public List<JavaPackage> getImportedPackages() {
        return new ArrayList<>(imports.values());
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    public void isAbstract(boolean isAbstract) {
        this.isAbstract = isAbstract;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof JavaClass otherClass) {
            return otherClass.getName().equals(getName());
        }
        return false;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public void setFinal(boolean aFinal) {
        isFinal = aFinal;
    }

    public boolean isInterface() {
        return isInterface;
    }

    public void setInterface(boolean anInterface) {
        isInterface = anInterface;
    }

    public List<Method> getMethods() {
        return methods;
    }

    public static class ClassComparator implements Comparator<JavaClass> {

        @Override
        public int compare(JavaClass o1, JavaClass o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }

    @Override
    public String toString() {
        return "com.dpgraph.parser.core.JavaClass{" + "className='" + className + '\'' +
                ", packageName='" + packageName + '\'' +
                ", isAbstract=" + isAbstract +
                ", imports=" + imports +
                ", sourceFile='" + sourceFile +
                ", isInterface='" + isInterface +
                ", isFinal='" + isFinal + '\'' +
                ", fields=" + fields +
                ", methods=" + methods +
                '}';
    }
}
