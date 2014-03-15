package com.hazelcast2.concurrent.lock.impl;

import com.hazelcast2.concurrent.lock.ILock;
import com.hazelcast2.concurrent.lock.LockConfig;
import com.hazelcast2.nio.IOUtils;
import com.hazelcast2.partition.PartitionService;
import com.hazelcast2.spi.InvocationEndpoint;
import com.hazelcast2.spi.PartitionAwareSpiService;
import com.hazelcast2.spi.SpiServiceSettings;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static com.hazelcast2.util.ReflectionUtils.getConstructor;

public final class LockService implements PartitionAwareSpiService {

    private static final String CLASS_NAME = "com.hazelcast2.concurrent.lock.impl.GeneratedLockSector";
    private static final Constructor<LockSector> CONSTRUCTOR = getConstructor(CLASS_NAME, LockSectorSettings.class);

    private final LockSector[] sectors;
    private final PartitionService partitionService;
    private final short serviceId;

    public LockService(SpiServiceSettings serviceSettings) {
        this.partitionService = serviceSettings.partitionService;
        this.serviceId = serviceSettings.serviceId;

        int partitionCount = partitionService.getPartitionCount();
        this.sectors = new LockSector[partitionCount];
        for (int partitionId = 0; partitionId < partitionCount; partitionId++) {
            sectors[partitionId] = newSector(serviceSettings, partitionId);
        }
    }

    private LockSector newSector(SpiServiceSettings serviceSettings, int partitionId) {
        LockSectorSettings sectorSettings = new LockSectorSettings();
        sectorSettings.service = this;
        sectorSettings.serializationService = serviceSettings.serializationService;
        sectorSettings.invocationCompletionService = serviceSettings.invocationCompletionService;
        sectorSettings.serviceId = serviceSettings.serviceId;
        sectorSettings.scheduler = partitionService.getScheduler();
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

    public ILock getDistributedObject(final LockConfig config) {
        if (config == null) {
            throw new NullPointerException("config can't be null");
        }

        final int partitionId = partitionService.getPartitionId(config.name);
        final LockSector sector = sectors[partitionId];
        final long id = sector.createCell();
        return new ILockProxy(sector, config.name, id);
    }

    @Override
    public void dispatch(final InvocationEndpoint source, final byte[] invocationBytes) {
        final int partitionId = getPartitionId(invocationBytes);
        final LockSector sector = sectors[partitionId];
        sector.schedule(source, invocationBytes);
    }

    private int getPartitionId(byte[] bytes) {
        return IOUtils.readInt(bytes, 2);
    }

    @Override
    public void enablePartition(int partitionId, boolean enable, InvocationEndpoint[] endpoints) {
        LockSector sector = sectors[partitionId];
        sector.endpoints = endpoints;
        if (enable) {
            sector.unlock();
        } else {
            sector.lock();
        }
    }
}