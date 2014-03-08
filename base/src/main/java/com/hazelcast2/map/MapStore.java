package com.hazelcast2.map;

public class MapStore {

    private final MapSector[] partitions;

    public MapStore(MapSector[] partitions) {
        this.partitions = partitions;
    }

    public MapSector getPartition(int partitionId) {
        return partitions[partitionId];
    }

    public int getPartitionCount() {
        return partitions.length;
    }
}
