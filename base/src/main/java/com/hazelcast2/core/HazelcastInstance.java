package com.hazelcast2.core;

public interface HazelcastInstance {

    ILock getLock(String name);

    IAtomicBoolean getAtomicBoolean(String name);

    IAtomicLong getAtomicLong(String name);

    <E> IAtomicReference<E> getAtomicReference(String name);

    IMap getMap(String name);

    void shutdown();
}
