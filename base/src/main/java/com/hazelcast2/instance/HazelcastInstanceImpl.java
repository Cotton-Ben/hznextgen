package com.hazelcast2.instance;

import com.hazelcast2.concurrent.atomicboolean.AtomicBooleanService;
import com.hazelcast2.concurrent.atomiclong.AtomicLongService;
import com.hazelcast2.core.HazelcastInstance;
import com.hazelcast2.core.IAtomicBoolean;
import com.hazelcast2.core.IAtomicLong;
import com.hazelcast2.core.ILock;
import com.hazelcast2.core.IMap;
import com.hazelcast2.concurrent.lock.LockService;

public class HazelcastInstanceImpl implements HazelcastInstance {

    private final AtomicLongService atomicLongService;
    private final AtomicBooleanService atomicBooleanService;
    private final LockService lockService;

    public HazelcastInstanceImpl() {
        int partitionCount = 271;
        this.atomicLongService = new AtomicLongService(partitionCount);
        this.atomicBooleanService = new AtomicBooleanService(partitionCount);
        this.lockService = new LockService(partitionCount);
    }

    @Override
    public ILock getLock(String name) {
        return null;
    }

    @Override
    public IAtomicBoolean getAtomicBoolean(String name) {
        return null;
    }

    @Override
    public IAtomicLong getAtomicLong(String name) {
        return null;
    }

    @Override
    public IMap getMap(String name) {
        return null;
    }
}
