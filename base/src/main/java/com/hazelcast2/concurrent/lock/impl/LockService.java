package com.hazelcast2.concurrent.lock.impl;

import com.hazelcast2.concurrent.lock.ILock;
import com.hazelcast2.concurrent.lock.LockConfig;
import com.hazelcast2.internal.nio.IOUtils;
import com.hazelcast2.partition.PartitionService;
import com.hazelcast2.spi.InvocationEndpoint;
import com.hazelcast2.spi.PartitionAwareSpiService;
import com.hazelcast2.spi.SpiServiceSettings;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static com.hazelcast2.internal.util.ReflectionUtils.getConstructor;
import static com.hazelcast2.internal.util.StringUtils.randomString;

public final class LockService implements PartitionAwareSpiService {

    public static final String SERVICE_NAME = "hz:impl:lockService";

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

    @Override
    public short getServiceId() {
        return serviceId;
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
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

    public ILock getDistributedObject(final LockConfig config) {
        if (config == null) {
            throw new NullPointerException("config can't be null");
        }

        if(config.name == null){
            config.name = randomString();
        }

        final int partitionId = partitionService.getPartitionId(config.name);
        final LockSector sector = sectors[partitionId];
        final long id = sector.hz_createCell(config);
        return new LockProxy(sector, config.name, id);
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
