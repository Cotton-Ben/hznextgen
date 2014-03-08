package com.hazelcast2.map;

public class MapStore {

    private final MapPartition[] partitions;

    public MapStore(MapPartition[] partitions) {
        this.partitions = partitions;
    }

    public MapPartition getPartition(int partitionId) {
        return partitions[partitionId];
    }

    public int getPartitionCount() {
        return partitions.length;
    }
}
