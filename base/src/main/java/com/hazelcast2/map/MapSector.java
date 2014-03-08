package com.hazelcast2.map;

import com.hazelcast2.spi.Sector;
import com.hazelcast2.spi.PartitionSettings;
import com.hazelcast2.spi.foo2.Foo2SectorAnnotation;

import java.util.HashMap;
import java.util.Map;

@Foo2SectorAnnotation
public abstract class MapSector extends Sector {

    private final Map<String, String> map = new HashMap<>();

    protected MapSector(PartitionSettings settings) {
        super(settings);
    }

    //todo: abstract
    public  String doGet(String key){return null;}

    //@Foo2OperationMethod
    public String get(String key) {
        return map.get(key);
    }

    //todo: abstract
    public  void doSet(String key, String value){}

    //@Foo2OperationMethod
    public void set(String key, String value) {
        map.put(key, value);
    }
}
