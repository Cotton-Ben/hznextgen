package com.hazelcast2.concurrent.atomicreference;

import com.hazelcast2.core.IAtomicReference;
import com.hazelcast2.partition.PartitionService;
import com.hazelcast2.spi.SpiService;
import com.hazelcast2.spi.SpiServiceSettings;
import com.hazelcast2.nio.IOUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static com.hazelcast2.util.ReflectionUtils.getConstructor;

public final class AtomicReferenceService implements SpiService {

    private static final String CLASS_NAME = "com.hazelcast2.concurrent.atomicreference.GeneratedReferenceSector";
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

    private ReferenceSector newSector(SpiServiceSettings serviceSettings, int partitionId) {
        ReferenceSectorSettings sectorSettings = new ReferenceSectorSettings();
        sectorSettings.scheduler = partitionService.getScheduler();
        sectorSettings.serializationService = serviceSettings.serializationService;
        sectorSettings.connectionManager = serviceSettings.connectionManager;
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

    public IAtomicReference getDistributedObject(final String name) {
        if (name == null) {
            throw new NullPointerException("name can't be null");
        }

        final int partitionId = partitionService.getPartitionId(name);
        final ReferenceSector partition = sectors[partitionId];
        final long id = partition.createCell();
        return new AtomicReferenceProxy(partition, name, id);
    }

    @Override
    public void schedule(final byte[] invocationBytes) {
        final int partitionId = getPartitionId(invocationBytes);
        final ReferenceSector sector = sectors[partitionId];
        sector.schedule(invocationBytes);
    }

    private int getPartitionId(byte[] bytes) {
        return IOUtils.readInt(bytes, 2);
    }
}
