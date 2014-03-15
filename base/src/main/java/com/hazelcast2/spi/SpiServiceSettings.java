package com.hazelcast2.spi;

import com.hazelcast2.core.config.Config;
import com.hazelcast2.partition.PartitionService;
import com.hazelcast2.serialization.SerializationService;

public class SpiServiceSettings {

    public PartitionService partitionService;
    public SerializationService serializationService;
    public InvocationCompletionService invocationCompletionService;
    public Config config;
    public short serviceId;
}
