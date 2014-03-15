package com.hazelcast2.core;

import com.hazelcast2.concurrent.atomicboolean.AtomicBooleanConfig;
import com.hazelcast2.concurrent.atomiclong.AtomicLongConfig;
import com.hazelcast2.concurrent.atomicreference.AtomicReferenceConfig;
import com.hazelcast2.concurrent.lock.LockConfig;
import com.hazelcast2.map.MapConfig;

import java.util.HashMap;
import java.util.Map;

public class Config {

    private Map<String, MapConfig> mapConfigs = new HashMap<>();
    private Map<String, AtomicReferenceConfig> atomicReferenceConfigs = new HashMap<>();
    private Map<String, AtomicLongConfig> atomicLongConfigs = new HashMap<>();
    private Map<String, AtomicBooleanConfig> atomicBooleanConfigs = new HashMap<>();
    private Map<String, LockConfig> lockConfigs = new HashMap<>();

    public int getPartitionCount() {
        return 271;
    }

    public AtomicBooleanConfig getAtomicBooleanConfig(String name) {
        AtomicBooleanConfig config = atomicBooleanConfigs.get(name);
        if (config == null) {
            config = new AtomicBooleanConfig();
            config.name = name;
        }
        return config;
    }

    public void addAtomicBooleanConfig(AtomicBooleanConfig config) {
        atomicBooleanConfigs.put(config.name, config);
    }

    public LockConfig getLockConfig(String name) {
        LockConfig config = lockConfigs.get(name);
        if (config == null) {
            config = new LockConfig();
            config.name = name;
        }
        return config;
    }

    public void addLockConfig(LockConfig config) {
        lockConfigs.put(config.name, config);
    }

    public AtomicLongConfig getAtomicLongConfig(String name) {
        AtomicLongConfig config = atomicLongConfigs.get(name);
        if (config == null) {
            config = new AtomicLongConfig();
            config.name = name;
        }
        return config;
    }

    public void addAtomicLongConfig(AtomicLongConfig config) {
        atomicLongConfigs.put(config.name, config);
    }

    public AtomicReferenceConfig getAtomicReferenceConfig(String name) {
        AtomicReferenceConfig config = atomicReferenceConfigs.get(name);
        if (config == null) {
            config = new AtomicReferenceConfig();
            config.name = name;
        }
        return config;
    }

    public void addAtomicReferenceConfig(AtomicReferenceConfig config) {
        atomicReferenceConfigs.put(config.name, config);
    }

    public Config addMapConfig(MapConfig mapConfig) {
        mapConfigs.put(mapConfig.name, mapConfig);
        return this;
    }

    public MapConfig getMapConfig(String name) {
        MapConfig config = mapConfigs.get(name);
        if (config == null) {
            config = new MapConfig();
            config.name = name;
        }
        return config;
    }
}
