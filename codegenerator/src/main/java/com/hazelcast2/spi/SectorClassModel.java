package com.hazelcast2.spi;

import java.util.LinkedList;
import java.util.List;

public class SectorClassModel {

    public String name;
    public String superName;
    public String packageName;
    public final List<SectorMethodModel> methods = new LinkedList<>();
    public String cellName;

    public String getCellName() {
        return cellName;
    }

    public String getName() {
        return name;
    }

    public String getSuperName() {
        return superName;
    }

    public String getPackageName() {
        return packageName;
    }

    public List<SectorMethodModel> getMethods() {
        return methods;
    }
}
