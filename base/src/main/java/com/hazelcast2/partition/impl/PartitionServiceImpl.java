package com.hazelcast2.partition.impl;

import com.hazelcast2.partition.PartitionService;
import com.hazelcast2.spi.PartitionScheduler;

public class PartitionServiceImpl implements PartitionService {
    private final int partitionCount;
    private final PartitionScheduler partitionScheduler;

    public PartitionServiceImpl(int partitionCount) {
        if (partitionCount < 1) {
            throw new IllegalArgumentException();
        }
        this.partitionCount = partitionCount;
        //todo: very small scheduler, needs to be made configurable.
        this.partitionScheduler = new PartitionScheduler(1024);
    }

    public int getPartitionCount() {
        return partitionCount;
    }

    @Override
    public PartitionScheduler getScheduler() {
        return partitionScheduler;
    }

    @Override
    public int getPartitionId(Object object) {
        if (object == null) {
            return 0;
        }

        int hash = object.hashCode();
        if (hash == Integer.MIN_VALUE) {
            hash = Integer.MIN_VALUE;
        } else if (hash < 0) {
            hash = -hash;
        }

        return hash % partitionCount;
    }
}
