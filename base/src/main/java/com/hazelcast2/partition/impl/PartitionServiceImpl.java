package com.hazelcast2.partition.impl;

import com.hazelcast2.partition.PartitionService;
import com.hazelcast2.spi.SectorScheduler;

public class PartitionServiceImpl implements PartitionService {
    private final int partitionCount;
    private final SectorScheduler sectorScheduler;

    public PartitionServiceImpl(int partitionCount) {
        if (partitionCount < 1) {
            throw new IllegalArgumentException();
        }
        this.partitionCount = partitionCount;
        //todo: very small scheduler, needs to be made configurable.
        int threadCount = 1;//Runtime.getRuntime().availableProcessors();
        int schedulerBufferSize = 1024;
        this.sectorScheduler = new SectorScheduler(schedulerBufferSize, threadCount);
        this.sectorScheduler.start();
    }

    @Override
    public int getPartitionCount() {
        return partitionCount;
    }

    @Override
    public SectorScheduler getScheduler() {
        return sectorScheduler;
    }

    @Override
    public void shutdown() {
        sectorScheduler.shutdown();
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
