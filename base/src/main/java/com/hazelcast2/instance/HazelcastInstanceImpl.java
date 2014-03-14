package com.hazelcast2.instance;

import com.hazelcast2.concurrent.atomicboolean.AtomicBooleanService;
import com.hazelcast2.concurrent.atomiclong.AtomicLongService;
import com.hazelcast2.concurrent.atomicreference.AtomicReferenceService;
import com.hazelcast2.concurrent.lock.LockService;
import com.hazelcast2.core.*;
import com.hazelcast2.map.MapService;
import com.hazelcast2.nio.ConnectionManager;
import com.hazelcast2.nio.Gateway;
import com.hazelcast2.nio.IOUtils;
import com.hazelcast2.nio.impl.ConnectionManagerImpl;
import com.hazelcast2.partition.PartitionService;
import com.hazelcast2.partition.impl.PartitionServiceImpl;
import com.hazelcast2.serialization.SerializationService;
import com.hazelcast2.spi.*;

import java.util.concurrent.atomic.AtomicBoolean;


/**
 * When an invocation is serialized, first the service id needs to be published. That will
 * be the first 16 bits. So when serialized operation is received, the first 16 bits can be
 * read to determine the service.
 */
public class HazelcastInstanceImpl implements HazelcastInstance, Gateway {

    private final PartitionService partitionService;
    private final SerializationService serializationService;
    private final AtomicBoolean shutdown = new AtomicBoolean();
    private final Config config;
    private final SpiService[] services;

    private final InvocationCompletionService invocationCompletionService;
    private final AtomicLongService atomicLongService;
    private final AtomicBooleanService atomicBooleanService;
    private final AtomicReferenceService atomicReferenceService;
    private final LockService lockService;
    private final MapService mapService;

    public HazelcastInstanceImpl(Config config) {
        this.config = config;
        this.partitionService = new PartitionServiceImpl(config.getPartitionCount());
        this.serializationService = new SerializationService();
        this.invocationCompletionService = new InvocationCompletionService((short)0);

        this.services = new SpiService[6];
        services[0]=invocationCompletionService;

        short serviceId = 1;
        this.atomicLongService = new AtomicLongService(newSpiServiceSettings(serviceId));
        services[serviceId] = atomicLongService;

        serviceId++;
        this.atomicBooleanService = new AtomicBooleanService(newSpiServiceSettings(serviceId));
        services[serviceId] = atomicBooleanService;

        serviceId++;
        this.atomicReferenceService = new AtomicReferenceService(newSpiServiceSettings(serviceId));
        services[serviceId] = atomicReferenceService;

        serviceId++;
        this.lockService = new LockService(newSpiServiceSettings(serviceId));
        services[serviceId] = lockService;

        serviceId++;
        this.mapService = new MapService(newSpiServiceSettings(serviceId));
        services[serviceId] = mapService;
    }

    public AtomicLongService getAtomicLongService() {
        return atomicLongService;
    }

    public AtomicBooleanService getAtomicBooleanService() {
        return atomicBooleanService;
    }

    public AtomicReferenceService getAtomicReferenceService() {
        return atomicReferenceService;
    }

    public LockService getLockService() {
        return lockService;
    }

    public MapService getMapService() {
        return mapService;
    }

    private SpiServiceSettings newSpiServiceSettings(short serviceId) {
        SpiServiceSettings dependencies = new SpiServiceSettings();
        dependencies.partitionService = partitionService;
        dependencies.serializationService = serializationService;
        dependencies.invocationCompletionService = invocationCompletionService;
        dependencies.config = config;
        dependencies.serviceId = serviceId;
        return dependencies;
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

    @Override
    public void startMaster() {
        for (int partitionId = 0; partitionId < partitionService.getPartitionCount(); partitionId++) {
            enablePartition(partitionId, true);
        }
    }

    @Override
    public void startAndJoin(HazelcastInstance master) {

    }

    private void enablePartition(int partitionId, boolean enable) {
        for (SpiService service : services) {
            if(service instanceof PartitionAwareSpiService){
                PartitionAwareSpiService partitionAwareSpiService = (PartitionAwareSpiService)service;
                partitionAwareSpiService.enablePartition(partitionId, enable);
            }
        }
    }

    @Override
    public void dispatch(final InvocationEndpoint source, final byte[] bytes) {
        final short serviceId = getServiceId(bytes);
        final SpiService spiService = services[serviceId];
        //here we have a polymorphic method call
        spiService.dispatch(source, bytes);
    }

    private short getServiceId(byte[] bytes) {
        return IOUtils.readShort(bytes, 0);
    }
}
