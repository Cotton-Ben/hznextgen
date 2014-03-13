package com.hazelcast2.spi;

import com.hazelcast2.nio.ConnectionManager;
import com.hazelcast2.partition.PartitionService;
import com.hazelcast2.serialization.SerializationService;

public class SectorSettings {
    public PartitionService partitionService;
    public SerializationService serializationService;
    public ConnectionManager connectionManager;

    public int partitionId;
    public SectorScheduler scheduler;
    public int ringbufferSize = 64;
    public short serviceId;
    public byte asyncBackupCount;
    public byte syncBackupCOunt;

    public SectorSettings(){}

    public SectorSettings(int partitionId, SectorScheduler scheduler) {
        this.partitionId = partitionId;
        this.scheduler = scheduler;
    }
}
