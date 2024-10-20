package com.dpgraph.javaparser.parser;

import com.dpgraph.javaparser.util.PackageFilter;
import com.dpgraph.javaparser.core.JavaClass;

import java.io.IOException;
import java.io.InputStream;

public abstract class Parser {
    private PackageFilter packageFilter;

    public Parser() {
        packageFilter = new PackageFilter();
    }

    public Parser(PackageFilter filter) {
        this.packageFilter = filter;
    }

    public abstract JavaClass parse(InputStream inputStream) throws IOException;

    protected PackageFilter getFilter() {
        if(packageFilter == null) {
            this.packageFilter = new PackageFilter();
        }

        return packageFilter;
    }
}
