package com.dpgraph.javaparser.parser;

import com.dpgraph.javaparser.core.Field;
import com.dpgraph.javaparser.core.JavaClass;
import com.dpgraph.javaparser.core.Method;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DependencyExtractor {
    private final Set<String> dependencies = new HashSet<>();


    public Set<String> extract(JavaClass javaClass) {
        for(Field field : javaClass.getFields()) {
            addDependency(field.type());
        }

        for(Method method : javaClass.getMethods()) {
            addDependency(method.returnType());

            for(String param : method.parameters()) {
                addDependency(param);
            }
        }

        dependencies.remove(javaClass.getName());
        dependencies.removeIf(dep -> dep.startsWith("java.") || dep.startsWith("javax."));

        return dependencies;
    }

    public void addDependency(String type) {
        type = type.replaceAll("\\[\\]", "");

        Pattern genericPattern = Pattern.compile("([^<]+)(<.*>)?");
        Matcher matcher = genericPattern.matcher(type);

        if(matcher.find()) {
            String baseType = matcher.group(1);

            if(baseType.equals("int") || baseType.equals("void") || baseType.equals("float") || baseType.equals("double") || baseType.equals("boolean") || baseType.equals("byte") || baseType.equals("char") || baseType.equals("short")) {
                return;
            }

            dependencies.add(baseType);

            String generic = matcher.group(2);
            if(generic != null) {
                addGenericDependencies(generic);
            }
        }
    }

    public void addGenericDependencies(String type) {
        type = type.substring(1, type.length()-1);

        Pattern splitPattern = Pattern.compile(",(?![^<>]*+>)");
        String[] genericTypes = splitPattern.split(type);

        for (String genericType : genericTypes) {
            addDependency(genericType.trim());
        }
    }

}
