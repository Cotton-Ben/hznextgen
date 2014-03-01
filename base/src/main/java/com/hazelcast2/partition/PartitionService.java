package com.hazelcast2.partition;

public interface PartitionService {

    int getPartitionId(Object object);

    int getPartitionCount();
}
