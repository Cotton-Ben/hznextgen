package com.hazelcast2.core;

import com.hazelcast2.concurrent.atomicboolean.AtomicBooleanConfig;
import com.hazelcast2.concurrent.atomicboolean.IAtomicBoolean;
import com.hazelcast2.concurrent.atomiclong.AtomicLongConfig;
import com.hazelcast2.concurrent.atomiclong.IAtomicLong;
import com.hazelcast2.concurrent.atomicreference.AtomicReferenceConfig;
import com.hazelcast2.concurrent.atomicreference.IAtomicReference;
import com.hazelcast2.concurrent.lock.ILock;
import com.hazelcast2.concurrent.lock.LockConfig;
import com.hazelcast2.map.IMap;
import com.hazelcast2.map.MapConfig;

public interface HazelcastInstance {

    ILock getLock(String name);

    ILock getLock(LockConfig config);

    IAtomicBoolean getAtomicBoolean(String name);

    IAtomicBoolean getAtomicBoolean(AtomicBooleanConfig config);

    IAtomicLong getAtomicLong(String name);

    IAtomicLong getAtomicLong(AtomicLongConfig config);

    <E> IAtomicReference<E> getAtomicReference(String name);

    <E> IAtomicReference<E> getAtomicReference(AtomicReferenceConfig config);

    IMap getMap(String name);

    IMap getMap(MapConfig config);

    void shutdown();

    //temp methods

    void startMaster();

    void startAndJoin(HazelcastInstance master);
}
