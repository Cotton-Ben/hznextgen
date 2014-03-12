package com.hazelcast2.map;

import com.hazelcast2.spi.SectorSettings;
import com.hazelcast2.spi.Sector;
import com.hazelcast2.spi.foo2.Foo2OperationMethod;
import com.hazelcast2.spi.foo2.Foo2SectorAnnotation;

import java.util.HashMap;
import java.util.Map;

@Foo2SectorAnnotation
public abstract class MapSector extends Sector {

    private final Map<String, String> map = new HashMap<>();

    protected MapSector(SectorSettings settings) {
        super(settings);
    }

    // ==================================================================================
    //                      get
    // ==================================================================================

    public abstract String doGet(String key);

    @Foo2OperationMethod(readonly = true)
    public String get(String key) {
        return map.get(key);
    }

    // ==================================================================================
    //                      set
    // ==================================================================================

    public abstract void doSet(String key, String value);

    @Foo2OperationMethod
    public void set(String key, String value) {
        map.put(key, value);
    }
}
