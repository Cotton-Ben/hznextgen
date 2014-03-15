package com.hazelcast2.map;

import com.hazelcast2.core.DistributedObject;

public interface IMap extends DistributedObject {

    String get(String key);

    void set(String key, String value);
}
