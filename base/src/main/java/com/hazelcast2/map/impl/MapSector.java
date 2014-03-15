package com.hazelcast2.map.impl;

import com.hazelcast2.spi.Sector;
import com.hazelcast2.spi.SectorSettings;
import com.hazelcast2.spi.SectorClass;
import com.hazelcast2.spi.SectorOperation;

import java.util.HashMap;
import java.util.Map;

@SectorClass
public abstract class MapSector extends Sector {

    private final Map<String, String> map = new HashMap<>();

    protected MapSector(SectorSettings settings) {
        super(settings);
    }

    // ==================================================================================
    //                      get
    // ==================================================================================

    public abstract String doGet(String key);

    @SectorOperation(readonly = true)
    public String get(String key) {
        return map.get(key);
    }

    // ==================================================================================
    //                      set
    // ==================================================================================

    public abstract void doSet(String key, String value);

    @SectorOperation
    public void set(String key, String value) {
        map.put(key, value);
    }
}
