package com.hazelcast2.spi;


public interface PartitionAwareSpiService extends SpiService {

    void enablePartition(int partitionId, boolean enable, InvocationEndpoint[] endpoints);
}
