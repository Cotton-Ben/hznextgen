package com.hazelcast2.concurrent.atomiclong;

import com.hazelcast2.core.IAtomicLong;
import com.hazelcast2.partition.PartitionService;
import com.hazelcast2.spi.SectorScheduler;
import com.hazelcast2.spi.SpiService;
import com.hazelcast2.spi.SpiServiceSettings;
import com.hazelcast2.util.IOUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static com.hazelcast2.util.ReflectionUtils.getConstructor;

public final class AtomicLongService implements SpiService {

    public static final String CLASS_NAME = "com.hazelcast2.concurrent.atomiclong.GeneratedLongSector";

    private final LongSector[] sectors;
    private final PartitionService partitionService;
    private final short serviceId;

    public AtomicLongService(SpiServiceSettings serviceSettings) {
        this.partitionService = serviceSettings.partitionService;
        this.serviceId = serviceSettings.serviceId;

        Constructor<LongSector> constructor = getConstructor(CLASS_NAME, LongSectorSettings.class);
        SectorScheduler scheduler = partitionService.getScheduler();

        int partitionCount = partitionService.getPartitionCount();
        sectors = new LongSector[partitionCount];
        for (int partitionId = 0; partitionId < partitionCount; partitionId++) {
            LongSectorSettings sectorSettings = new LongSectorSettings();
            sectorSettings.scheduler = scheduler;
            sectorSettings.service = this;
            sectorSettings.serviceId = serviceSettings.serviceId;
            sectorSettings.partitionId = partitionId;
            sectorSettings.serializationService = serviceSettings.serializationService;
            try {
                LongSector partition = constructor.newInstance(sectorSettings);
                sectors[partitionId] = partition;
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public short getServiceId() {
        return serviceId;
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

    @Override
    public void schedule(final byte[] invocationBytes) {
        final int partitionId = getPartitionId(invocationBytes);
        final LongSector sector = sectors[partitionId];
        sector.schedule(invocationBytes);
    }

    private int getPartitionId(byte[] bytes) {
        return IOUtils.readInt(bytes, 2);
    }
}
