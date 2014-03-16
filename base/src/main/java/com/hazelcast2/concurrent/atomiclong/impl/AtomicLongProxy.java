package com.hazelcast2.concurrent.atomiclong.impl;

import com.hazelcast2.concurrent.atomiclong.IAtomicLong;
import com.hazelcast2.core.LongFunction;

import java.util.concurrent.Future;

public final class AtomicLongProxy implements IAtomicLong {

    private final LongSector sector;
    private final long id;
    private final String name;

    public AtomicLongProxy(LongSector sector, String name, long id) {
        this.sector = sector;
        this.name = name;
        this.id = id;
    }

    public LongSector getSector(){
        return sector;
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
    public long get() {
        return sector.hz_get(id);
    }

    @Override
    public Future<Long> asyncGet() {
        return sector.hz_asyncGet(id);
    }

    @Override
    public void set(long newValue) {
        sector.hz_set(id, newValue);
    }

    @Override
    public Future<Void> asyncSet(long update) {
        return sector.hz_asyncSet(id, update);
    }

    @Override
    public void inc() {
        sector.hz_inc(id);
    }

    @Override
    public Future<Void> asyncInc() {
        return sector.hz_asyncInc(id);
    }

    @Override
    public boolean compareAndSet(long expect, long update) {
        return sector.hz_compareAndSet(id, expect, update);
    }

    @Override
    public Future<Boolean> asyncCompareAndSet(long expect, long update) {
        return sector.hz_asyncCompareAndSet(id, expect, update);
    }

    @Override
    public long apply(LongFunction f) {
        return sector.hz_apply(id, f);
    }

    @Override
    public Future<Long> asyncApply(LongFunction f) {
        return sector.hz_asyncApply(id, f);
    }

     @Override
    public void alter(LongFunction f) {
        sector.hz_alter(id, f);
    }

    @Override
    public Future<Void> asyncAlter(LongFunction f) {
        return sector.hz_asyncAlter(id, f);
    }

    @Override
    public void destroy() {
        sector.hz_destroy(id);
    }

    @Override
    public boolean isDestroyed() {
        return sector.hz_isDestroyed(id)!=1;
    }

    @Override
    public String toString() {
        return "IAtomicLong{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
