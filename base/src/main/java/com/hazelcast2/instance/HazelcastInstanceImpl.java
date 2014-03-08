package com.hazelcast2.instance;

import com.hazelcast2.concurrent.atomicboolean.AtomicBooleanService;
import com.hazelcast2.concurrent.atomiclong.AtomicLongService;
import com.hazelcast2.concurrent.lock.LockService;
import com.hazelcast2.core.HazelcastInstance;
import com.hazelcast2.core.IAtomicBoolean;
import com.hazelcast2.core.IAtomicLong;
import com.hazelcast2.core.ILock;
import com.hazelcast2.core.IMap;
import com.hazelcast2.map.MapService;
import com.hazelcast2.partition.PartitionService;
import com.hazelcast2.partition.impl.PartitionServiceImpl;

public class HazelcastInstanceImpl implements HazelcastInstance {

    private final PartitionService partitionService;
    private final AtomicLongService atomicLongService;
    private final AtomicBooleanService atomicBooleanService;
    private final LockService lockService;
    private final MapService mapService;

    public HazelcastInstanceImpl() {
        int partitionCount = 271;
        this.partitionService = new PartitionServiceImpl(partitionCount);
        this.atomicLongService = new AtomicLongService(partitionService);
        this.atomicBooleanService = new AtomicBooleanService(partitionService);
        this.lockService = new LockService(partitionService);
        this.mapService = new MapService(partitionService);
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
    public IAtomicLong getAtomicLong(String name) {
        return atomicLongService.getDistributedObject(name);
    }

    @Override
    public IMap getMap(String name) {
        return mapService.getDistributedObject(name);
    }
}
