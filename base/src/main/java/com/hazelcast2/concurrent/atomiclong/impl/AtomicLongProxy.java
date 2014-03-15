package com.hazelcast2.concurrent.atomiclong.impl;

import com.hazelcast2.concurrent.atomiclong.IAtomicLong;
import com.hazelcast2.core.LongFunction;

import java.util.concurrent.Future;

public class AtomicLongProxy implements IAtomicLong {

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
    public long get() {
        return sector.doGet(id);
    }

    @Override
    public Future<Long> asyncGet() {
        return sector.asyncDoGet(id);
    }

    @Override
    public void set(long newValue) {
        sector.doSet(id, newValue);
    }

    @Override
    public Future<Void> asyncSet(long update) {
        return sector.asyncDoSet(id, update);
    }

    @Override
    public void inc() {
        sector.doInc(id);
    }

    @Override
    public Future<Void> asyncInc() {
        return sector.asyncDoInc(id);
    }

    @Override
    public boolean compareAndSet(long expect, long update) {
        return sector.doCompareAndSet(id, expect, update);
    }

    @Override
    public Future<Boolean> asyncCompareAndSet(long expect, long update) {
        return sector.asyncDoCompareAndSet(id, expect, update);
    }

    @Override
    public long apply(LongFunction f) {
        return sector.doApply(id, f);
    }

    @Override
    public Future<Long> asyncApply(LongFunction f) {
        return sector.asyncDoApply(id, f);
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void alter(LongFunction f) {
        sector.doAlter(id, f);
    }

    @Override
    public Future<Void> asyncAlter(LongFunction f) {
        return sector.asyncDoAlter(id, f);
    }

    @Override
    public String toString() {
        return "IAtomicLong{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
