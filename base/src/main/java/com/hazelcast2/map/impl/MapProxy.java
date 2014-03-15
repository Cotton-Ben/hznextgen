package com.hazelcast2.map.impl;

import com.hazelcast2.map.IMap;

public class MapProxy implements IMap {

    private final MapStore mapStore;
    private final String name;
    private final long id;

    public MapProxy(MapStore mapStore, long id, String name) {
        this.mapStore = mapStore;
        this.id = id;
        this.name = name;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
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

    @Override
    public void destroy() {
        throw new UnsupportedOperationException("todo");
    }

    @Override
    public boolean isDestroyed() {
        throw new UnsupportedOperationException("todo");
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

    @Override
    public String toString() {
        return "IMap{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
