package com.hazelcast2.map.impl;

import com.hazelcast2.map.IMap;

public class MapProxy implements IMap {

    private final MapStore mapStore;

    public MapProxy(MapStore mapStore) {
        this.mapStore = mapStore;
    }

    @Override
    public long getId() {
        return -1;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String get(String key) {
        if (key == null) {
            throw new NullPointerException("key can't be null");
        }

        MapSector sector = getSector(key);
        return sector.doGet(key);
    }

    @Override
    public void set(String key, String value) {
        if (key == null) {
            throw new NullPointerException("key can't be null");
        }

        MapSector sector = getSector(key);
        sector.doSet(key, value);
    }

    public MapSector getSector(String key) {
        int partitionId = getPartitionId(key);
        return mapStore.getSector(partitionId);
    }

    private int getPartitionId(String key) {
        //todo: needs to be forwarded to another system instead of doing here.
        int hash = key.hashCode();
        if (hash == Integer.MIN_VALUE) {
            hash = Integer.MAX_VALUE;
        } else if (hash < 0) {
            hash = -hash;
        }

        return hash % mapStore.getPartitionCount();
    }
}
