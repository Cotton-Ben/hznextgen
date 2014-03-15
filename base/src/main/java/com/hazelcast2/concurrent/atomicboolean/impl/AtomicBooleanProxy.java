package com.hazelcast2.concurrent.atomicboolean.impl;

import com.hazelcast2.concurrent.atomicboolean.IAtomicBoolean;

import java.util.concurrent.Future;

public class AtomicBooleanProxy implements IAtomicBoolean {

    private final BooleanSector sector;
    private final long id;
    private final String name;

    public AtomicBooleanProxy(BooleanSector sector, String name, long id) {
        this.sector = sector;
        this.name = name;
        this.id = id;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean get() {
        return sector.doGet(id);
    }

    @Override
    public Future<Boolean> asyncGet() {
        return sector.asyncDoGet(id);
    }

    @Override
    public boolean set(boolean update) {
        return sector.doSet(id, update);
    }

    @Override
    public Future<Boolean> asyncSet(boolean update) {
        return sector.asyncDoSet(id, update);
    }

    @Override
    public boolean compareAndSet(boolean old, boolean update) {
        return sector.doCompareAndSet(id, old, update);
    }

    @Override
    public Future<Boolean> asyncCompareAndSet(boolean old, boolean update) {
        return sector.asyncDoCompareAndSet(id, old, update);
    }

    @Override
    public void destroy() {
        sector.doDestroy(id);
    }

    @Override
    public boolean isDestroyed() {
        return sector.doIsDestroyed(id)!=1;
    }

    @Override
    public String toString() {
        return "IAtomicBoolean{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
