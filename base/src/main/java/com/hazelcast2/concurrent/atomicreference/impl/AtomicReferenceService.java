package com.hazelcast2.concurrent.atomicreference.impl;

import com.hazelcast2.concurrent.atomicreference.AtomicReferenceConfig;
import com.hazelcast2.concurrent.atomicreference.IAtomicReference;
import com.hazelcast2.internal.nio.IOUtils;
import com.hazelcast2.partition.PartitionService;
import com.hazelcast2.spi.InvocationEndpoint;
import com.hazelcast2.spi.PartitionAwareSpiService;
import com.hazelcast2.spi.SpiServiceSettings;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static com.hazelcast2.internal.util.ReflectionUtils.getConstructor;
import static com.hazelcast2.internal.util.StringUtils.randomString;

public final class AtomicReferenceService implements PartitionAwareSpiService {

    public static final String SERVICE_NAME = "hz:impl:atomicReferenceService";

    private static final String CLASS_NAME = "com.hazelcast2.concurrent.atomicreference.impl.GeneratedReferenceSector";
    private static final Constructor<ReferenceSector> CONSTRUCTOR = getConstructor(CLASS_NAME, ReferenceSectorSettings.class);

    private final ReferenceSector[] sectors;
    private final PartitionService partitionService;
    private final short serviceId;

    public AtomicReferenceService(SpiServiceSettings serviceSettings) {
        this.partitionService = serviceSettings.partitionService;
        this.serviceId = serviceSettings.serviceId;

        int partitionCount = partitionService.getPartitionCount();
        this.sectors = new ReferenceSector[partitionCount];
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

    private ReferenceSector newSector(SpiServiceSettings serviceSettings, int partitionId) {
        ReferenceSectorSettings sectorSettings = new ReferenceSectorSettings();
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

    public IAtomicReference getDistributedObject(final AtomicReferenceConfig config) {
        if (config == null) {
            throw new NullPointerException("name can't be null");
        }

        if (config.name == null) {
            config.name = randomString();
        }

        final int partitionId = partitionService.getPartitionId(config.name);
        final ReferenceSector sector = sectors[partitionId];
        final long id = sector.createCell(config);
        return new AtomicReferenceProxy(sector, config.name, id);
    }

    @Override
    public void dispatch(final InvocationEndpoint source, final byte[] invocationBytes) {
        final int partitionId = getPartitionId(invocationBytes);
        final ReferenceSector sector = sectors[partitionId];
        sector.schedule(source, invocationBytes);
    }

    private int getPartitionId(byte[] bytes) {
        return IOUtils.readInt(bytes, 2);
    }

    @Override
    public void enablePartition(int partitionId, boolean enable, InvocationEndpoint[] endpoints) {
        ReferenceSector sector = sectors[partitionId];
        sector.endpoints = endpoints;
        if (enable) {
            sector.unlock();
        } else {
            sector.lock();
        }
    }
}
