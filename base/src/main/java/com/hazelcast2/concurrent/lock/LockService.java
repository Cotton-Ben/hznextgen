package com.hazelcast2.concurrent.lock;

import com.hazelcast2.core.ILock;
import com.hazelcast2.partition.PartitionService;
import com.hazelcast2.spi.SectorScheduler;
import com.hazelcast2.spi.SpiService;
import com.hazelcast2.spi.SpiServiceSettings;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static com.hazelcast2.util.ReflectionUtils.getConstructor;

public final class LockService implements SpiService {

    public static final String CLASS_NAME = "com.hazelcast2.concurrent.lock.GeneratedLockSector";

    private final LockSector[] sectors;
    private final PartitionService partitionService;

    public LockService(SpiServiceSettings serviceSettings) {
        this.partitionService = serviceSettings.partitionService;

        Constructor<LockSector> constructor = getConstructor(CLASS_NAME, LockSectorSettings.class);
        SectorScheduler scheduler = partitionService.getScheduler();

        int partitionCount = partitionService.getPartitionCount();
        sectors = new LockSector[partitionCount];
        for (int partitionId = 0; partitionId < partitionCount; partitionId++) {
            LockSectorSettings sectorSettings = new LockSectorSettings();
            sectorSettings.service = this;
            sectorSettings.serializationService = serviceSettings.serializationService;
            sectorSettings.serviceId = serviceSettings.serviceId;
            sectorSettings.scheduler = scheduler;
            sectorSettings.partitionId = partitionId;
            try {
                LockSector partition = constructor.newInstance(sectorSettings);
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

    @Override
    public void schedule(byte[] invocationBytes) {
        throw new UnsupportedOperationException();
    }
}
