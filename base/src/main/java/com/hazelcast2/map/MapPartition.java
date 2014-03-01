package com.hazelcast2.map;

import com.hazelcast2.spi.OperationMethod;
import com.hazelcast2.spi.AbstractPartition;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class MapPartition extends AbstractPartition {

    private final ConcurrentMap<String, String> map = new ConcurrentHashMap<String, String>();

    protected MapPartition(int partitionId) {
        super(null);
    }

    public abstract String doGet(String key);

    @OperationMethod
    public String get(String key) {
        return map.get(key);
    }

    public abstract void doSet(String key, String value);

    @OperationMethod
    public void set(String key, String value) {
        map.put(key, value);
    }
}
