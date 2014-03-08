package com.hazelcast2.partition;

import com.hazelcast2.spi.PartitionScheduler;

public interface PartitionService {

    int getPartitionId(Object object);

    int getPartitionCount();

    PartitionScheduler getScheduler();
}
