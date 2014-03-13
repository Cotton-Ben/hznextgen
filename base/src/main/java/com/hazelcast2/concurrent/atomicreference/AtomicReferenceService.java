package com.hazelcast2.concurrent.atomicreference;

import com.hazelcast2.core.IAtomicReference;
import com.hazelcast2.partition.PartitionService;
import com.hazelcast2.spi.SectorScheduler;
import com.hazelcast2.spi.SpiService;
import com.hazelcast2.spi.SpiServiceSettings;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static com.hazelcast2.util.ReflectionUtils.getConstructor;

public final class AtomicReferenceService implements SpiService {

    public static final String CLASS_NAME = "com.hazelcast2.concurrent.atomicreference.GeneratedReferenceSector";

    private final ReferenceSector[] sectors;
    private final PartitionService partitionService;
    private final short serviceId;

    public AtomicReferenceService(SpiServiceSettings serviceSettings) {
        this.partitionService = serviceSettings.partitionService;
        this.serviceId = serviceSettings.serviceId;

        Constructor<ReferenceSector> constructor = getConstructor(CLASS_NAME,ReferenceSectorSettings.class);
        SectorScheduler scheduler = partitionService.getScheduler();

        int partitionCount = partitionService.getPartitionCount();
        sectors = new ReferenceSector[partitionCount];
        for (int partitionId = 0; partitionId < partitionCount; partitionId++) {
            ReferenceSectorSettings sectorSettings = new ReferenceSectorSettings();
            sectorSettings.partitionId = partitionId;
            sectorSettings.scheduler = scheduler;
            sectorSettings.serializationService = serviceSettings.serializationService;
            sectorSettings.serviceId = serviceSettings.serviceId;
            sectorSettings.service = this;
            try {
                ReferenceSector partition = constructor.newInstance(sectorSettings);
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
    public void schedule(byte[] invocationBytes) {
        throw new UnsupportedOperationException();
    }
}
