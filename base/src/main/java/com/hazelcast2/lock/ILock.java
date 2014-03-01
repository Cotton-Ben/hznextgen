package com.hazelcast2.lock;

public interface ILock {

    boolean tryLock();

    boolean isLocked();

    void unlock();

    long getId();
}
