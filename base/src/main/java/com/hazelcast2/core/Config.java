package com.hazelcast2.core;

import java.util.HashMap;
import java.util.Map;

public class Config {

    private Map<String, MapConfig> mapConfigs = new HashMap<String, MapConfig>();

    public Config addMapConfig(MapConfig mapConfig) {
        mapConfigs.put(mapConfig.getName(), mapConfig);
        return this;
    }

    public MapConfig getMapConfig(String name){
        MapConfig config = mapConfigs.get(name);
        if(config == null){
            config = new MapConfig("default");
        }
        return config;
    }
}
