package com.hazelcast2.concurrent.lock.impl;

import com.hazelcast2.concurrent.lock.ILock;

public class LockProxy implements ILock {

    private final LockSector sector;
    private final long id;
    private final String name;

    public LockProxy(LockSector sector, String name, long id) {
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
    public void lock() throws InterruptedException {
         sector.hz_lock(id, LockUtil.getThreadId());
    }

    @Override
    public boolean tryLock() {
        return sector.hz_tryLock(id, LockUtil.getThreadId());
    }

    @Override
    public boolean isLocked() {
        return sector.hz_isLocked(id, LockUtil.getThreadId());
    }

    @Override
    public void unlock() {
        sector.hz_unlock(id, LockUtil.getThreadId());
    }

    @Override
    public void destroy() {
        sector.hz_destroy(id);
    }

    @Override
    public boolean isDestroyed() {
        return sector.hz_isDestroyed(id) != 1;
    }

    @Override
    public String toString() {
        return "ILock{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
