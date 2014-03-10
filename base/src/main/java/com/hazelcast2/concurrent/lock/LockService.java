package com.hazelcast2.concurrent.lock;

import com.hazelcast2.core.Config;
import com.hazelcast2.core.ILock;
import com.hazelcast2.partition.PartitionService;
import com.hazelcast2.spi.PartitionSettings;
import com.hazelcast2.spi.SectorScheduler;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static com.hazelcast2.util.ReflectionUtils.getConstructor;

public final class LockService {

    public static final String CLASS_NAME = "com.hazelcast2.concurrent.lock.GeneratedLockSector";

    private final LockSector[] sectors;
    private final PartitionService partitionService;

    public LockService(PartitionService partitionService, Config config) {
        this.partitionService = partitionService;

        Constructor<LockSector> constructor = getConstructor(CLASS_NAME);
        SectorScheduler scheduler = partitionService.getScheduler();

        int partitionCount = partitionService.getPartitionCount();
        sectors = new LockSector[partitionCount];
        for (int partitionId = 0; partitionId < partitionCount; partitionId++) {
            PartitionSettings settings = new PartitionSettings(partitionId, scheduler);
            try {
                LockSector partition = constructor.newInstance(settings);
                sectors[partitionId] = partition;
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public ILock getDistributedObject(final String name) {
        if (name == null) {
            throw new NullPointerException("name can't be null");
        }

        final int partitionId = partitionService.getPartitionId(name);
        final LockSector partition = sectors[partitionId];
        final long id = partition.createCell();
        return new ILockProxy(partition, name, id);
    }
}
