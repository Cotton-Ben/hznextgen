package com.hazelcast2.spi;

import com.hazelcast2.partition.PartitionService;
import com.hazelcast2.serialization.SerializationService;

public class SectorSettings {
    public PartitionService partitionService;
    public SerializationService serializationService;
    public InvocationCompletionService invocationCompletionService;

    public int partitionId;
    public SectorScheduler scheduler;
    public int ringbufferSize = 64;
    public short serviceId;

    public SectorSettings(){}

    public SectorSettings(int partitionId, SectorScheduler scheduler) {
        this.partitionId = partitionId;
        this.scheduler = scheduler;
    }
}
