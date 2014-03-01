package com.hazelcast2.concurrent.atomicboolean;

import com.hazelcast2.core.IAtomicBoolean;

import java.util.concurrent.Future;

public class AtomicBooleanProxy implements IAtomicBoolean {

    private final BooleanPartition partition;
    private final long id;
    private final String name;

    public AtomicBooleanProxy(BooleanPartition partition, String name, long id) {
        this.partition = partition;
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

    @Override
    public String toString() {
        return "IAtomicLong{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
