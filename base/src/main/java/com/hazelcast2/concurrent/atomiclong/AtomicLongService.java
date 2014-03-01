package com.hazelcast2.concurrent.atomiclong;

import com.hazelcast2.spi.PartitionSettings;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static com.hazelcast2.util.ReflectionUtils.getConstructor;

public class AtomicLongService {

    public static final String CLASS_NAME = "com.hazelcast2.concurrent.atomiclong.GeneratedLongPartition";

    private final LongPartition[] partitions;

    public AtomicLongService(int partitionCount) {
        Constructor<LongPartition> constructor = getConstructor(CLASS_NAME);

        partitions = new LongPartition[partitionCount];
        for (int partitionId = 0; partitionId < partitionCount; partitionId++) {
            PartitionSettings settings = new PartitionSettings(partitionId);
            try {
                LongPartition partition = constructor.newInstance(settings);
                partitions[partitionId] = partition;
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public LongPartition getPartition(final int partitionId) {
        return partitions[partitionId];
    }
}
