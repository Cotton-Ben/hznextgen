package com.hazelcast2.concurrent.atomiclong;

import com.hazelcast2.core.IAtomicLong;

import java.util.concurrent.Future;

public class AtomicLongProxy implements IAtomicLong {

    private final LongSector partition;
    private final long id;
    private final String name;

    public AtomicLongProxy(LongSector partition, String name, long id) {
        this.partition = partition;
        this.name = name;
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long get() {
        return partition.doGet(id);
    }

    @Override
    public Future<Long> asyncGet() {
        return partition.asyncDoGet(id);
    }

    @Override
    public void set(long newValue) {
        partition.doSet(id, newValue);
    }

    @Override
    public Future<Void> asyncSet(long update) {
        return partition.asyncDoSet(id, update);
    }

    @Override
    public void inc() {
        partition.doInc(id);
    }

    @Override
    public Future<Void> asyncInc() {
        return partition.asyncDoInc(id);
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String toString() {
        return "IAtomicLong{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
