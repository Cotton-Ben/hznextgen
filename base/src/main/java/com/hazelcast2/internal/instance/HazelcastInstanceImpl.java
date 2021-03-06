package com.hazelcast2.internal.instance;

import com.hazelcast2.internal.cluster.Cluster;
import com.hazelcast2.internal.cluster.ClusterSettings;
import com.hazelcast2.concurrent.atomicboolean.AtomicBooleanConfig;
import com.hazelcast2.concurrent.atomicboolean.IAtomicBoolean;
import com.hazelcast2.concurrent.atomicboolean.impl.AtomicBooleanService;
import com.hazelcast2.concurrent.atomiclong.AtomicLongConfig;
import com.hazelcast2.concurrent.atomiclong.IAtomicLong;
import com.hazelcast2.concurrent.atomiclong.impl.AtomicLongService;
import com.hazelcast2.concurrent.atomicreference.AtomicReferenceConfig;
import com.hazelcast2.concurrent.atomicreference.impl.AtomicReferenceService;
import com.hazelcast2.concurrent.atomicreference.IAtomicReference;
import com.hazelcast2.concurrent.lock.ILock;
import com.hazelcast2.concurrent.lock.LockConfig;
import com.hazelcast2.concurrent.lock.impl.LockService;
import com.hazelcast2.core.*;
import com.hazelcast2.map.IMap;
import com.hazelcast2.map.MapConfig;
import com.hazelcast2.map.impl.MapService;
import com.hazelcast2.internal.nio.Gateway;
import com.hazelcast2.internal.nio.IOUtils;
import com.hazelcast2.partition.PartitionService;
import com.hazelcast2.partition.impl.PartitionServiceImpl;
import com.hazelcast2.serialization.SerializationService;
import com.hazelcast2.spi.*;

import java.util.HashMap;
import java.util.Map;
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
    private final Cluster cluster;

    public HazelcastInstanceImpl(Config config) {
        this.config = config;
        this.partitionService = new PartitionServiceImpl(config.getPartitionCount());
        this.serializationService = new SerializationService();

        short serviceId = 0;

        this.invocationCompletionService = new InvocationCompletionService(serviceId);

        serviceId++;
        ClusterSettings clusterSettings = new ClusterSettings();
        clusterSettings.serviceId  = serviceId;
        clusterSettings.serializationService = serializationService;
        this.cluster = new Cluster(clusterSettings);
        cluster.start();

        this.services = new SpiService[7];
        services[invocationCompletionService.getServiceId()] = invocationCompletionService;
        services[cluster.getServiceId()] = cluster;

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
        LockConfig lockConfig = config.getLockConfig(name);
        return lockService.getDistributedObject(lockConfig);
    }

    @Override
    public ILock getLock(LockConfig config) {
        return lockService.getDistributedObject(config);
    }

    @Override
    public IAtomicBoolean getAtomicBoolean(String name) {
        AtomicBooleanConfig booleanConfig = config.getAtomicBooleanConfig(name);
        return atomicBooleanService.getDistributedObject(booleanConfig);
    }

    @Override
    public IAtomicBoolean getAtomicBoolean(AtomicBooleanConfig config) {
        return atomicBooleanService.getDistributedObject(config);
    }

    @Override
    public <E> IAtomicReference<E> getAtomicReference(String name) {
        AtomicReferenceConfig referenceConfig = config.getAtomicReferenceConfig(name);
        return atomicReferenceService.getDistributedObject(referenceConfig);
    }

    @Override
    public <E> IAtomicReference<E> getAtomicReference(AtomicReferenceConfig config) {
        return atomicReferenceService.getDistributedObject(config);
    }

    @Override
    public IAtomicLong getAtomicLong(String name) {
        AtomicLongConfig longConfig = config.getAtomicLongConfig(name);
        return atomicLongService.getDistributedObject(longConfig);
    }

    @Override
    public IAtomicLong getAtomicLong(AtomicLongConfig config) {
       return atomicLongService.getDistributedObject(config);
    }

    @Override
    public IMap getMap(String name) {
        MapConfig mapConfig = config.getMapConfig(name);
        return mapService.getDistributedObject(mapConfig);
    }

    @Override
    public IMap getMap(MapConfig config) {
        return mapService.getDistributedObject(config);
    }

    @Override
    public void shutdown() {
        //if it is already shutdown, we don't need to shut it down again
        if (!shutdown.compareAndSet(false, true)) {
            return;
        }

        partitionService.shutdown();
        cluster.shutdown();
        //todo: we need to shutdown the services.
    }

    public boolean ownsPartition(int partitionId) {
        return partitionOwns.get(partitionId);
    }


    @Override
    public void startMaster() {
        for (int partitionId = 0; partitionId < partitionService.getPartitionCount(); partitionId++) {
            enablePartition(partitionId, true);
        }
    }

    @Override
    public void startAndJoin(HazelcastInstance master) {
        HazelcastInstanceImpl m = (HazelcastInstanceImpl) master;

        DirectInvocationEndpoint e1 = new DirectInvocationEndpoint(this);
        DirectInvocationEndpoint e2 = new DirectInvocationEndpoint(m);
        e1.source = e2;
        e2.source = e1;

        for (int partitionId = 0; partitionId < partitionService.getPartitionCount(); partitionId++) {
            if (partitionId % 2 == 0) {
                enablePartition(partitionId, true);
                m.enablePartition(partitionId, false, e1);
            } else {
                m.enablePartition(partitionId, true);
                enablePartition(partitionId, false, e2);
            }
        }
    }

    private Map<Integer, Boolean> partitionOwns = new HashMap<>();

    private void enablePartition(int partitionId, boolean enable, InvocationEndpoint... endpoints) {
        partitionOwns.put(partitionId, enable);

        for (SpiService service : services) {
            if (service instanceof PartitionAwareSpiService) {
                PartitionAwareSpiService partitionAwareSpiService = (PartitionAwareSpiService) service;
                partitionAwareSpiService.enablePartition(partitionId, enable, endpoints);
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

    public SerializationService getSerializationService() {
        return serializationService;
    }
}
