package com.hazelcast2.concurrent.lock;

import com.hazelcast2.core.ILock;

import static com.hazelcast2.concurrent.lock.LockUtil.getThreadId;

public class ILockProxy implements ILock {

    private final LockSector sector;
    private final long id;
    private final String name;

    public ILockProxy(LockSector sector, String name, long id) {
        this.sector = sector;
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
        return sector.doTryLock(id, getThreadId());
    }

    @Override
    public boolean isLocked() {
        return sector.doIsLocked(id, getThreadId());
    }

    @Override
    public void unlock() {
        sector.doUnlock(id, getThreadId());
    }

    @Override
    public String toString() {
        return "ILock{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
