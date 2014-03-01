package com.hazelcast2.core;

public interface ILock extends DistributedObject {

    boolean tryLock();

    boolean isLocked();

    void unlock();

    long getId();
}
