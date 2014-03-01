package com.hazelcast2.concurrent.lock;

import com.hazelcast2.spi.PartitionSettings;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static com.hazelcast2.util.ReflectionUtils.getConstructor;

public class LockService {
    public static final String CLASS_NAME = "com.hazelcast2.concurrent.lock.GeneratedLockPartition";

    private final LockPartition[] partitions;

    public LockService(int partitionCount) {
        Constructor<LockPartition> constructor = getConstructor(CLASS_NAME);

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
}
