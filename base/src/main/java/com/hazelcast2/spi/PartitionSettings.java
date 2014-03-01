package com.hazelcast2.spi;

import com.hazelcast2.partition.impl.PartitionServiceImpl;

public class PartitionSettings {
    public int partitionId;
    public PartitionScheduler scheduler = new PartitionScheduler();
    public int ringbufferSize = 64;
    public PartitionServiceImpl partitionService;

    public PartitionSettings(int partitionId) {
        this.partitionId = partitionId;
    }

    public int getPartitionId() {
        return partitionId;
    }
}
