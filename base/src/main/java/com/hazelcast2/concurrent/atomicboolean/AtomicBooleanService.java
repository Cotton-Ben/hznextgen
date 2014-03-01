package com.hazelcast2.concurrent.atomicboolean;

import com.hazelcast2.spi.PartitionSettings;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static com.hazelcast2.util.ReflectionUtils.getConstructor;

public class AtomicBooleanService {

    public static final String CLASS_NAME = "com.hazelcast2.concurrent.atomicboolean.GeneratedBooleanPartition";

    private final BooleanPartition[] partitions;

    public AtomicBooleanService(int partitionCount) {
        Constructor<BooleanPartition> constructor = getConstructor(CLASS_NAME);

        partitions = new BooleanPartition[partitionCount];
        for (int partitionId = 0; partitionId < partitionCount; partitionId++) {
            PartitionSettings settings = new PartitionSettings(partitionId);
            try {
                BooleanPartition partition = constructor.newInstance(settings);
                partitions[partitionId] = partition;
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public BooleanPartition getPartition(final int partitionId) {
        return partitions[partitionId];
    }
}
