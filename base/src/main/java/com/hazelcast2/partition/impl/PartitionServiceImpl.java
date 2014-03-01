package com.hazelcast2.partition.impl;

import com.hazelcast2.partition.PartitionService;

public class PartitionServiceImpl implements PartitionService {
    private final int partitionCount;

    public PartitionServiceImpl(int partitionCount) {
        if (partitionCount < 1) {
            throw new IllegalArgumentException();
        }
        this.partitionCount = partitionCount;
    }

    public int getPartitionCount() {
        return partitionCount;
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
