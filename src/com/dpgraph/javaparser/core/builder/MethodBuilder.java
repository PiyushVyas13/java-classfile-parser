package com.dpgraph.javaparser.core.builder;

import com.dpgraph.javaparser.core.Method;
import com.dpgraph.javaparser.parser.Constant;
import com.dpgraph.javaparser.parser.FieldOrMethodInfo;
import com.dpgraph.javaparser.util.Modifier;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MethodBuilder extends BaseAttributeBuilder<Method> {

    public MethodBuilder(Constant[] pool) {
        super(pool);
    }

    @Override
    public Method parse(FieldOrMethodInfo poolEntry) {

        String descriptor = parseDescriptor(poolEntry);

        Modifier modifier = parseModifier(poolEntry);


        boolean isStatic = parseFlag(poolEntry, ACC_STATIC);
        boolean isAbstract = parseFlag(poolEntry, ACC_ABSTRACT);
        boolean isFinal = parseFlag(poolEntry, ACC_FINAL);

        Pattern pattern = Pattern.compile("\\((.*)\\)(.+)");
        Matcher matcher = pattern.matcher(descriptor);

        String returnType = null;
        String name = getName(poolEntry);

        List<String> parameters = new ArrayList<>();
        if (matcher.find()) {
            String params = matcher.group(1);
            returnType = parseType(matcher.group(2));



            String[] list = params.split(";");
            for(String str : list) {
                if(str.indexOf('<') != -1 && !str.endsWith(">")) {
                    str = str + '>';
                }
            }

            for (String str : list) {
                if (!str.isEmpty()) {
                    if(str.equals(">")) {
                        continue;
                    }
                    String param = parseType(str + ";");
                    parameters.add(param);
                }
            }

        }

        return new Method(modifier, isStatic, isFinal, isAbstract, name, parameters, returnType);
    }

}
