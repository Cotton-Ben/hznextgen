package com.hazelcast2.spi;

public class PartitionSettings {
    public int partitionId;
    public Scheduler scheduler = new Scheduler();
    public int ringbufferSize = 64;

    public PartitionSettings(int partitionId) {
        this.partitionId = partitionId;
    }

    public int getPartitionId() {
        return partitionId;
    }
}
