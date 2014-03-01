package com.hazelcast2.atomicboolean;

import com.hazelcast2.core.IAtomicBoolean;

import java.util.concurrent.Future;

public class AtomicBooleanProxy implements IAtomicBoolean {

    private final BooleanPartition partition;
    private final long id;

    public AtomicBooleanProxy(BooleanPartition partition) {
        this.partition = partition;
        this.id = partition.createCell();
    }

    public AtomicBooleanProxy(BooleanPartition partition, long id) {
        this.partition = partition;
        this.id = id;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public boolean get() {
        return partition.doGet(id);
    }

    @Override
    public Future<Boolean> asyncGet() {
        return partition.asyncDoGet(id);
    }

    @Override
    public boolean set(boolean update) {
        return partition.doSet(id, update);
    }

    @Override
    public Future<Boolean> asyncSet(boolean update) {
        return partition.asyncDoSet(id, update);
    }

    @Override
    public boolean compareAndSet(boolean old, boolean update) {
        return partition.doCompareAndSet(id, old, update);
    }

    @Override
    public Future<Boolean> asyncCompareAndSet(boolean old, boolean update) {
        return partition.asyncDoCompareAndSet(id, old, update);
    }
}
