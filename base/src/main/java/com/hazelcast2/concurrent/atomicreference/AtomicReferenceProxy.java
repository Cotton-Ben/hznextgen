package com.hazelcast2.concurrent.atomicreference;

import com.hazelcast2.core.IAtomicReference;

import java.util.concurrent.Future;

public class AtomicReferenceProxy<E> implements IAtomicReference<E> {

    private final ReferenceSector sector;
    private final long id;
    private final String name;

    public AtomicReferenceProxy(ReferenceSector sector, String name, long id) {
        this.sector = sector;
        this.name = name;
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public E get() {
        return (E)sector.doGet(id);
    }

    @Override
    public Future<E> asyncGet() {
        return (Future<E>)sector.asyncDoGet(id);
    }

    @Override
    public void set(E update) {
        sector.doSet(id, update);
    }

    @Override
    public Future<Void> asyncSet(E update) {
       return sector.asyncDoSet(id, update);
    }

    @Override
    public boolean compareAndSet(E expect, E update) {
       return sector.doCompareAndSet(id, expect, update);
    }

    @Override
    public Future<Boolean> asyncCompareAndSet(E expect, E update) {
        return sector.asyncDoCompareAndSet(id, expect, update);
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
