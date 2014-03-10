package com.hazelcast2.concurrent.atomiclong;

import com.hazelcast2.core.Config;
import com.hazelcast2.core.IAtomicLong;
import com.hazelcast2.partition.PartitionService;
import com.hazelcast2.spi.PartitionSettings;
import com.hazelcast2.spi.SectorScheduler;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static com.hazelcast2.util.ReflectionUtils.getConstructor;

public final class AtomicLongService {

    public static final String CLASS_NAME = "com.hazelcast2.concurrent.atomiclong.GeneratedLongSector";

    private final LongSector[] sectors;
    private final PartitionService partitionService;

    public AtomicLongService(PartitionService partitionService, Config config) {
        this.partitionService = partitionService;

        Constructor<LongSector> constructor = getConstructor(CLASS_NAME);
        SectorScheduler scheduler = partitionService.getScheduler();

        int partitionCount = partitionService.getPartitionCount();
        sectors = new LongSector[partitionCount];
        for (int partitionId = 0; partitionId < partitionCount; partitionId++) {
            PartitionSettings settings = new PartitionSettings(partitionId, scheduler);
            try {
                LongSector partition = constructor.newInstance(settings);
                sectors[partitionId] = partition;
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
        final LongSector partition = sectors[partitionId];
        final long id = partition.createCell();
        return new AtomicLongProxy(partition, name, id);
    }
}
