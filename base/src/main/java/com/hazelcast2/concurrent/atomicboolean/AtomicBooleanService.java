package com.hazelcast2.concurrent.atomicboolean;

import com.hazelcast2.core.IAtomicBoolean;
import com.hazelcast2.nio.IOUtils;
import com.hazelcast2.partition.PartitionService;
import com.hazelcast2.spi.SpiService;
import com.hazelcast2.spi.SpiServiceSettings;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static com.hazelcast2.util.ReflectionUtils.getConstructor;

public final class AtomicBooleanService implements SpiService {

    private static final String CLASS_NAME = "com.hazelcast2.concurrent.atomicboolean.GeneratedBooleanSector";
    private static final Constructor<BooleanSector> CONSTRUCTOR = getConstructor(CLASS_NAME, BooleanSectorSettings.class);

    private final BooleanSector[] sectors;
    private final PartitionService partitionService;
    private final short serviceId;

    public AtomicBooleanService(SpiServiceSettings serviceSettings) {
        this.partitionService = serviceSettings.partitionService;
        this.serviceId = serviceSettings.serviceId;

        int partitionCount = partitionService.getPartitionCount();
        this.sectors = new BooleanSector[partitionCount];
        for (int partitionId = 0; partitionId < partitionCount; partitionId++) {
            sectors[partitionId] = newSector(serviceSettings, partitionId);
        }
    }

    private BooleanSector newSector(SpiServiceSettings serviceSettings, int partitionId) {
        BooleanSectorSettings sectorSettings = new BooleanSectorSettings();
        sectorSettings.scheduler = partitionService.getScheduler();
        sectorSettings.serializationService = serviceSettings.serializationService;
        sectorSettings.partitionId = partitionId;
        sectorSettings.serviceId = serviceSettings.serviceId;
        sectorSettings.service = this;
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
    public void schedule(final byte[] invocationBytes) {
        final int partitionId = getPartitionId(invocationBytes);
        final BooleanSector sector = sectors[partitionId];
        sector.schedule(invocationBytes);
    }

    private int getPartitionId(byte[] bytes) {
        return IOUtils.readInt(bytes, 2);
    }
}
