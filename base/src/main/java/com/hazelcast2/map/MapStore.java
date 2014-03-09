package com.hazelcast2.map;

public class MapStore {

    private final MapSector[] sectors;

    public MapStore(MapSector[] partitions) {
        this.sectors = partitions;
    }

    public MapSector getSector(int partitionId) {
        return sectors[partitionId];
    }

    public int getPartitionCount() {
        return sectors.length;
    }
}
