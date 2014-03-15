package com.hazelcast2.concurrent.lock;

import com.hazelcast2.core.DistributedObject;

public interface ILock extends DistributedObject {

    boolean tryLock();

    boolean isLocked();

    void lock() throws InterruptedException;

    void unlock();

    long getId();
}
