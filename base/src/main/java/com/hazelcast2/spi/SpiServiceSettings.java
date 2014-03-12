package com.hazelcast2.spi;

import com.hazelcast2.core.Config;
import com.hazelcast2.partition.PartitionService;
import com.hazelcast2.serialization.SerializationService;

public class SpiServiceSettings {

    public PartitionService partitionService;
    public SerializationService serializationService;
    public Config config;
    public short serviceId;
}
