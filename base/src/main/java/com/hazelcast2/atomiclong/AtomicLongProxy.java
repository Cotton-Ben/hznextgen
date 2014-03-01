package com.hazelcast2.atomiclong;

import com.hazelcast2.IAtomicLong;

import java.util.concurrent.Future;

public class AtomicLongProxy implements IAtomicLong {

    private final LongPartition partition;
    private final long id;

    public AtomicLongProxy(LongPartition partition) {
        this.partition = partition;
        this.id = partition.createCell();
    }

    public AtomicLongProxy(LongPartition partition, long id) {
        this.partition = partition;
        this.id = id;
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
}
