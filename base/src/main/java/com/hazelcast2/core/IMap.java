package com.hazelcast2.core;

public interface IMap extends DistributedObject {

    String get(String key);

    void set(String key, String value);
}
