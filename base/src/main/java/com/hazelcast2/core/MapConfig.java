package com.hazelcast2.core;

public class MapConfig {

    private String name;

    public MapConfig(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
