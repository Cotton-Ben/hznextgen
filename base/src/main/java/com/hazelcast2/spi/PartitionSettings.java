package com.hazelcast2.spi;

import com.hazelcast2.partition.PartitionService;

public class PartitionSettings {
    public int partitionId;
    public SectorScheduler scheduler;
    public int ringbufferSize = 64;
    public PartitionService partitionService;

    public PartitionSettings(int partitionId, SectorScheduler scheduler) {
        this.partitionId = partitionId;
        this.scheduler = scheduler;
    }

    public int getPartitionId() {
        return partitionId;
    }
}
