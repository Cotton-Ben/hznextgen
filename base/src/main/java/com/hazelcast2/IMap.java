package com.hazelcast2;

public interface IMap {

    String get(String key);

    void set(String key, String value);
}
