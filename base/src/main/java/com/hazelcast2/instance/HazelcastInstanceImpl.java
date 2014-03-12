package com.hazelcast2.instance;

import com.hazelcast2.concurrent.atomicboolean.AtomicBooleanService;
import com.hazelcast2.concurrent.atomiclong.AtomicLongService;
import com.hazelcast2.concurrent.atomicreference.AtomicReferenceService;
import com.hazelcast2.concurrent.lock.LockService;
import com.hazelcast2.core.*;
import com.hazelcast2.map.MapService;
import com.hazelcast2.partition.PartitionService;
import com.hazelcast2.partition.impl.PartitionServiceImpl;

import java.util.concurrent.atomic.AtomicBoolean;

public class HazelcastInstanceImpl implements HazelcastInstance {

    private static final short SERVICE_ID_LONG = 1;
    private static final short SERVICE_ID_BOOLEAN = 2;
    private static final short SERVICE_ID_REFERENCE = 3;
    private static final short SERVICE_ID_LOCK = 4;
    private static final short SERVICE_ID_MAP = 5;

    private final PartitionService partitionService;
    private final AtomicLongService atomicLongService;
    private final AtomicBooleanService atomicBooleanService;
    private final AtomicReferenceService atomicReferenceService;
    private final LockService lockService;
    private final MapService mapService;
    private final AtomicBoolean shutdown = new AtomicBoolean();

    public HazelcastInstanceImpl(){
        this(new Config());
    }

    public HazelcastInstanceImpl(Config config) {
        int partitionCount = 271;
        this.partitionService = new PartitionServiceImpl(partitionCount);

        this.atomicLongService = new AtomicLongService(partitionService, config, SERVICE_ID_LONG);
        this.atomicBooleanService = new AtomicBooleanService(partitionService, config, SERVICE_ID_BOOLEAN);
        this.atomicReferenceService = new AtomicReferenceService(partitionService, config,SERVICE_ID_REFERENCE);
        this.lockService = new LockService(partitionService, config, SERVICE_ID_LOCK);
        this.mapService = new MapService(partitionService, config, SERVICE_ID_MAP);
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
}
