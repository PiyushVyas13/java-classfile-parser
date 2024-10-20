package com.dpgraph.javaparser.util;

import java.util.ArrayList;
import java.util.List;

public class PackageFilter {
    private final List<String> filteredPackages;

    public PackageFilter() {
        this.filteredPackages = new ArrayList<>();
        PropertyConfigurator configurator = new PropertyConfigurator();
        for(String packageName : configurator.getFilteredPackages()) {
            if(packageName.endsWith("*")) {
                packageName = packageName.substring(0, packageName.length()-1);
            }

            if(!packageName.isEmpty()) {
                filteredPackages.add(packageName);
            }
        }
    }

    public List<String> getFilteredPackageNames() {
        return filteredPackages;
    }

    public boolean accept(String packageName) {
        for(String name : filteredPackages) {
            if(packageName.startsWith(name)) {
                return false;
            }
        }

        return true;
    }
}
