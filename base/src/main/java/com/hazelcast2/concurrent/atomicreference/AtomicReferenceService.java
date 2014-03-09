package com.hazelcast2.concurrent.atomicreference;

import com.hazelcast2.core.IAtomicReference;
import com.hazelcast2.partition.PartitionService;
import com.hazelcast2.spi.PartitionSettings;
import com.hazelcast2.spi.SectorScheduler;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static com.hazelcast2.util.ReflectionUtils.getConstructor;

public final class AtomicReferenceService {

    public static final String CLASS_NAME = "com.hazelcast2.concurrent.atomicreference.GeneratedReferenceSector";

    private final ReferenceSector[] sectors;
    private final PartitionService partitionService;

    public AtomicReferenceService(PartitionService partitionService) {
        this.partitionService = partitionService;

        Constructor<ReferenceSector> constructor = getConstructor(CLASS_NAME);
        SectorScheduler scheduler = partitionService.getScheduler();

        int partitionCount = partitionService.getPartitionCount();
        sectors = new ReferenceSector[partitionCount];
        for (int partitionId = 0; partitionId < partitionCount; partitionId++) {
            PartitionSettings settings = new PartitionSettings(partitionId, scheduler);
            try {
                ReferenceSector partition = constructor.newInstance(settings);
                sectors[partitionId] = partition;
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
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
}
