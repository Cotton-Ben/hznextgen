package com.hazelcast2.concurrent.atomicboolean.impl;

import com.hazelcast2.concurrent.atomicboolean.AtomicBooleanConfig;
import com.hazelcast2.concurrent.atomicboolean.IAtomicBoolean;
import com.hazelcast2.nio.IOUtils;
import com.hazelcast2.partition.PartitionService;
import com.hazelcast2.spi.InvocationEndpoint;
import com.hazelcast2.spi.PartitionAwareSpiService;
import com.hazelcast2.spi.SpiServiceSettings;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static com.hazelcast2.util.ReflectionUtils.getConstructor;

public final class AtomicBooleanService implements PartitionAwareSpiService {

    private static final String CLASS_NAME = "com.hazelcast2.concurrent.atomicboolean.impl.GeneratedBooleanSector";
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
        sectorSettings.invocationCompletionService = serviceSettings.invocationCompletionService;
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

    public IAtomicBoolean getDistributedObject(final AtomicBooleanConfig config) {
        if (config == null) {
            throw new NullPointerException("config can't be null");
        }

        final int partitionId = partitionService.getPartitionId(config.name);
        final BooleanSector sector = sectors[partitionId];
        final long id = sector.createCell();
        return new AtomicBooleanProxy(sector, config.name, id);
    }

    @Override
    public void dispatch(final InvocationEndpoint source, final byte[] invocationBytes) {
        final int partitionId = getPartitionId(invocationBytes);
        final BooleanSector sector = sectors[partitionId];
        sector.schedule(source, invocationBytes);
    }

    private int getPartitionId(byte[] bytes) {
        return IOUtils.readInt(bytes, 2);
    }

    @Override
    public void enablePartition(int partitionId, boolean enable, InvocationEndpoint[] endpoints) {
        BooleanSector sector = sectors[partitionId];
        sector.endpoints = endpoints;
        if (enable) {
            sector.unlock();
        } else {
            sector.lock();
        }
    }
}
