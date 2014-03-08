package com.hazelcast2.partition;

import com.hazelcast2.spi.SectorScheduler;

public interface PartitionService {

    int getPartitionId(Object object);

    int getPartitionCount();

    SectorScheduler getScheduler();

    void shutdown();
}
