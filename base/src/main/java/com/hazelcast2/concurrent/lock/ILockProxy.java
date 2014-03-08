package com.hazelcast2.concurrent.lock;

import com.hazelcast2.core.ILock;

import static com.hazelcast2.concurrent.lock.LockUtil.getThreadId;

public class ILockProxy implements ILock {

    private final LockSector partition;
    private final long id;
    private final String name;

    public ILockProxy(LockSector partition, String name, long id) {
        this.partition = partition;
        this.name = name;
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public boolean tryLock() {
        return partition.doTryLock(id, getThreadId());
    }

    @Override
    public boolean isLocked() {
        return partition.doIsLocked(id, getThreadId());
    }

    @Override
    public void unlock() {
        partition.doUnlock(id, getThreadId());
    }

    @Override
    public String toString() {
        return "ILock{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
