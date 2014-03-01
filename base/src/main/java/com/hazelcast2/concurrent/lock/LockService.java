package com.hazelcast2.concurrent.lock;

import com.hazelcast2.core.ILock;
import com.hazelcast2.partition.PartitionService;
import com.hazelcast2.spi.PartitionSettings;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static com.hazelcast2.util.ReflectionUtils.getConstructor;

public final class LockService {

    public static final String CLASS_NAME = "com.hazelcast2.concurrent.lock.GeneratedLockPartition";

    private final LockPartition[] partitions;
    private final PartitionService partitionService;

    public LockService(PartitionService partitionService) {
        this.partitionService = partitionService;

        Constructor<LockPartition> constructor = getConstructor(CLASS_NAME);
        int partitionCount = partitionService.getPartitionCount();
        partitions = new LockPartition[partitionCount];
        for (int partitionId = 0; partitionId < partitionCount; partitionId++) {
            PartitionSettings settings = new PartitionSettings(partitionId);
            try {
                LockPartition partition = constructor.newInstance(settings);
                partitions[partitionId] = partition;
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public LockPartition getPartition(final int partitionId) {
        return partitions[partitionId];
    }

    public ILock getDistributedObject(final String name) {
        if (name == null) {
            throw new NullPointerException("name can't be null");
        }

        final int partitionId = partitionService.getPartitionId(name);
        final LockPartition partition = partitions[partitionId];
        final long id = partition.createCell();
        return new ILockProxy(partition, name, id);
    }
}
