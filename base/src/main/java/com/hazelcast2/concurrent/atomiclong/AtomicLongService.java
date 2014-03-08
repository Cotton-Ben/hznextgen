package com.hazelcast2.concurrent.atomiclong;

import com.hazelcast2.core.IAtomicLong;
import com.hazelcast2.partition.PartitionService;
import com.hazelcast2.spi.PartitionScheduler;
import com.hazelcast2.spi.PartitionSettings;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static com.hazelcast2.util.ReflectionUtils.getConstructor;

public final class AtomicLongService {

    public static final String CLASS_NAME = "com.hazelcast2.concurrent.atomiclong.GeneratedLongPartition";

    private final LongPartition[] partitions;
    private final PartitionService partitionService;

    public AtomicLongService(PartitionService partitionService) {
        this.partitionService = partitionService;

        Constructor<LongPartition> constructor = getConstructor(CLASS_NAME);
        PartitionScheduler scheduler = partitionService.getScheduler();

        int partitionCount = partitionService.getPartitionCount();
        partitions = new LongPartition[partitionCount];
        for (int partitionId = 0; partitionId < partitionCount; partitionId++) {
            PartitionSettings settings = new PartitionSettings(partitionId,scheduler);
            try {
                LongPartition partition = constructor.newInstance(settings);
                partitions[partitionId] = partition;
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public IAtomicLong getDistributedObject(final String name) {
        if (name == null) {
            throw new NullPointerException("name can't be null");
        }

        final int partitionId = partitionService.getPartitionId(name);
        final LongPartition partition = partitions[partitionId];
        final long id = partition.createCell();
        return new AtomicLongProxy(partition, name, id);
    }
}
