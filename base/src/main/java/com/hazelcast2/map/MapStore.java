package com.hazelcast2.map;

import com.hazelcast2.core.MapConfig;

public class MapStore {

    private final MapSector[] sectors;

    public MapStore(MapSector[] partitions, MapConfig mapConfig) {
        this.sectors = partitions;
    }

    public MapSector getSector(int partitionId) {
        return sectors[partitionId];
    }

    public int getPartitionCount() {
        return sectors.length;
    }
}
