package com.hazelcast2.instance;

import com.hazelcast2.concurrent.atomicboolean.AtomicBooleanService;
import com.hazelcast2.concurrent.atomiclong.AtomicLongService;
import com.hazelcast2.concurrent.atomicreference.AtomicReferenceService;
import com.hazelcast2.concurrent.lock.LockService;
import com.hazelcast2.core.*;
import com.hazelcast2.map.MapService;
import com.hazelcast2.partition.PartitionService;
import com.hazelcast2.partition.impl.PartitionServiceImpl;
import com.hazelcast2.spi.SpiService;
import com.hazelcast2.util.IOUtils;

import java.util.concurrent.atomic.AtomicBoolean;


/**
 * When an invocation is serialized, first the service id needs to be published. That will
 * be the first 16 bits. So when serialized operation is received, the first 16 bits can be
 * read to determine the service.
 */
public class HazelcastInstanceImpl implements HazelcastInstance {

    private final PartitionService partitionService;
    private final AtomicLongService atomicLongService;
    private final AtomicBooleanService atomicBooleanService;
    private final AtomicReferenceService atomicReferenceService;
    private final LockService lockService;
    private final MapService mapService;
    private final AtomicBoolean shutdown = new AtomicBoolean();
    private final SpiService[] services;

    public HazelcastInstanceImpl(Config config) {
        //todo: should come from the config
        int partitionCount = 271;
        this.partitionService = new PartitionServiceImpl(partitionCount);

        this.services = new SpiService[5];

        short k = 0;
        this.atomicLongService = new AtomicLongService(partitionService, config, k);
        services[k] = atomicLongService;

        k++;
        this.atomicBooleanService = new AtomicBooleanService(partitionService, config, k);
        services[k] = atomicBooleanService;

        k++;
        this.atomicReferenceService = new AtomicReferenceService(partitionService, config, k);
        services[k] = atomicReferenceService;


        k++;
        this.lockService = new LockService(partitionService, config, k);
        services[k] = lockService;

        k++;
        this.mapService = new MapService(partitionService, config, k);
        services[k] = mapService;
    }

    @Override
    public ILock getLock(String name) {
        return lockService.getDistributedObject(name);
    }

    @Override
    public IAtomicBoolean getAtomicBoolean(String name) {
        return atomicBooleanService.getDistributedObject(name);
    }

    @Override
    public <E> IAtomicReference<E> getAtomicReference(String name) {
        return atomicReferenceService.getDistributedObject(name);
    }

    @Override
    public IAtomicLong getAtomicLong(String name) {
        return atomicLongService.getDistributedObject(name);
    }

    @Override
    public IMap getMap(String name) {
        return mapService.getDistributedObject(name);
    }

    @Override
    public void shutdown() {
        //if it is already shutdown, we don't need to shut it down again
        if (!shutdown.compareAndSet(false, true)) {
            return;
        }

        partitionService.shutdown();
        //todo: we need to shutdown the services.
    }

    public void dispatch(final byte[] bytes) {
        final short serviceId = getServiceId(bytes);
        final SpiService spiService = services[serviceId];
        //here we have a polymorphic method call
        spiService.schedule(bytes);
    }

    private short getServiceId(byte[] bytes) {
        return IOUtils.readShort(bytes, 0);
    }
}
