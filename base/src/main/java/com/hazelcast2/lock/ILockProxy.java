package com.hazelcast2.lock;

import static com.hazelcast2.lock.LockUtil.getThreadId;

public class ILockProxy implements ILock {

    private final LockPartition partition;
    private final long id;

    public ILockProxy(LockPartition partition) {
        this.partition = partition;
        this.id = partition.createCell();
    }

    public ILockProxy(LockPartition partition, long id) {
        this.partition = partition;
        this.id = id;
    }

    @Override
    public boolean tryLock() {
        return partition.doTryLock(id, getThreadId());
    }

    @Override
    public boolean isLocked() {
        return partition.doIsLocked(id,getThreadId());
    }

    @Override
    public void unlock() {
        partition.doUnlock(id,getThreadId());
    }

    @Override
    public long getId() {
        return id;
    }
}
