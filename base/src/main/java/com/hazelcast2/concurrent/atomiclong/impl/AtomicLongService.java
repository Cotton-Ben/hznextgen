package com.hazelcast2.concurrent.atomiclong.impl;

import com.hazelcast2.concurrent.atomiclong.AtomicLongConfig;
import com.hazelcast2.concurrent.atomiclong.IAtomicLong;
import com.hazelcast2.internal.nio.IOUtils;
import com.hazelcast2.partition.PartitionService;
import com.hazelcast2.spi.InvocationEndpoint;
import com.hazelcast2.spi.PartitionAwareSpiService;
import com.hazelcast2.spi.SpiServiceSettings;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static com.hazelcast2.internal.util.ReflectionUtils.getConstructor;
import static com.hazelcast2.internal.util.StringUtils.randomString;

public final class AtomicLongService implements PartitionAwareSpiService {

    private static final String CLASS_NAME = "com.hazelcast2.concurrent.atomiclong.impl.GeneratedLongSector";
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
        sectorSettings.invocationCompletionService = serviceSettings.invocationCompletionService;
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

    public IAtomicLong getDistributedObject(final AtomicLongConfig config) {
        if (config == null) {
            throw new NullPointerException("config can't be null");
        }

        if(config.name == null){
            config.name = randomString();
        }

        final int partitionId = partitionService.getPartitionId(config.name);
        final LongSector sector = sectors[partitionId];
        final long id = sector.createCell(config);
        return new AtomicLongProxy(sector, config.name, id);
    }

    @Override
    public void dispatch(final InvocationEndpoint source, final byte[] invocationBytes) {
        final int partitionId = getPartitionId(invocationBytes);
        final LongSector sector = sectors[partitionId];
        sector.schedule(source, invocationBytes);
    }

    private int getPartitionId(byte[] bytes) {
        return IOUtils.readInt(bytes, 2);
    }

    @Override
    public void enablePartition(int partitionId, boolean enable, InvocationEndpoint[] endpoints) {
        LongSector sector = sectors[partitionId];
        sector.endpoints = endpoints;
        if (enable) {
            sector.unlock();
        } else {
            sector.lock();
        }
    }
}
