package com.hazelcast2.core;

public interface HazelcastInstance {

    ILock getLock(String name);

    IAtomicBoolean getAtomicBoolean(String name);

    IAtomicLong getAtomicLong(String name);

    IMap getMap(String name);

    void shutdown();
}
