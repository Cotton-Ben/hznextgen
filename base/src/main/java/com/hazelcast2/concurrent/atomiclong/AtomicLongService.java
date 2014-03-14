package com.hazelcast2.concurrent.atomiclong;

import com.hazelcast2.concurrent.atomicboolean.BooleanSector;
import com.hazelcast2.core.IAtomicLong;
import com.hazelcast2.partition.PartitionService;
import com.hazelcast2.spi.SpiService;
import com.hazelcast2.spi.SpiServiceSettings;
import com.hazelcast2.nio.IOUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static com.hazelcast2.util.ReflectionUtils.getConstructor;

public final class AtomicLongService implements SpiService {

    private static final String CLASS_NAME = "com.hazelcast2.concurrent.atomiclong.GeneratedLongSector";
    private static final Constructor<LongSector> CONSTRUCTOR = getConstructor(CLASS_NAME, LongSectorSettings.class);

    private final LongSector[] sectors;
    private final PartitionService partitionService;
    private final short serviceId;

    public AtomicLongService(SpiServiceSettings serviceSettings) {
        this.partitionService = serviceSettings.partitionService;
        this.serviceId = serviceSettings.serviceId;

        int partitionCount = partitionService.getPartitionCount();
        this.sectors = new LongSector[partitionCount];
        for (int partitionId = 0; partitionId < partitionCount; partitionId++) {
            sectors[partitionId] = newSector(serviceSettings, partitionId);
        }
    }

    private LongSector newSector(SpiServiceSettings serviceSettings, int partitionId) {
        LongSectorSettings sectorSettings = new LongSectorSettings();
        sectorSettings.scheduler = partitionService.getScheduler();
        sectorSettings.serializationService = serviceSettings.serializationService;
        sectorSettings.service = this;
        sectorSettings.serviceId = serviceSettings.serviceId;
        sectorSettings.partitionId = partitionId;
        try {
            return CONSTRUCTOR.newInstance(sectorSettings);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
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

    @Override
    public void enablePartition(int partitionId, boolean enable) {
        LongSector sector = sectors[partitionId];
        if (enable) {
            sector.unlock();
        } else {
            sector.lock();
        }
    }
}
