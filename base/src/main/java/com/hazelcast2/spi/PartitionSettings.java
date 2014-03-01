package com.hazelcast2.spi;

public class PartitionSettings {
    public int segmentCount;
    public int partitionId;
    public Scheduler scheduler = new Scheduler();

    public PartitionSettings(int segmentCount, int partitionId) {
        this.segmentCount = segmentCount;
        this.partitionId = partitionId;
    }

    public int getSegmentCount() {
        return segmentCount;
    }

    public int getPartitionId() {
        return partitionId;
    }
}
