package com.hazelcast2.concurrent.atomicboolean;

import com.hazelcast2.core.Config;
import com.hazelcast2.core.IAtomicBoolean;
import com.hazelcast2.partition.PartitionService;
import com.hazelcast2.spi.SectorScheduler;
import com.hazelcast2.spi.SectorSettings;
import com.hazelcast2.spi.SpiService;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static com.hazelcast2.util.ReflectionUtils.getConstructor;

public final class AtomicBooleanService implements SpiService {

    public static final String CLASS_NAME = "com.hazelcast2.concurrent.atomicboolean.GeneratedBooleanSector";

    private final BooleanSector[] sectors;
    private final PartitionService partitionService;

    public AtomicBooleanService(PartitionService partitionService, Config config, short serviceId) {
        this.partitionService = partitionService;

        Constructor<BooleanSector> constructor = getConstructor(CLASS_NAME, BooleanSectorSettings.class);

        int partitionCount = partitionService.getPartitionCount();
        sectors = new BooleanSector[partitionCount];
        SectorScheduler scheduler = partitionService.getScheduler();
        for (int partitionId = 0; partitionId < partitionCount; partitionId++) {
            BooleanSectorSettings settings = new BooleanSectorSettings();
            settings.partitionId = partitionId;
            settings.scheduler = scheduler;
            settings.serviceId = serviceId;
            settings.service = this;
            try {
                BooleanSector partition = constructor.newInstance(settings);
                sectors[partitionId] = partition;
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public IAtomicBoolean getDistributedObject(final String name) {
        if (name == null) {
            throw new NullPointerException("name can't be null");
        }

        final int partitionId = partitionService.getPartitionId(name);
        final BooleanSector partition = sectors[partitionId];
        final long id = partition.createCell();
        return new AtomicBooleanProxy(partition, name, id);
    }

    @Override
    public void schedule(byte[] invocationBytes) {
        throw new UnsupportedOperationException();
    }
}
