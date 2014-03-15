package com.hazelcast2.core;

import com.hazelcast2.core.config.*;

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
