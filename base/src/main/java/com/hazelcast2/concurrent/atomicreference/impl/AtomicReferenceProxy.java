package com.hazelcast2.concurrent.atomicreference.impl;

import com.hazelcast2.concurrent.atomicreference.IAtomicReference;
import com.hazelcast2.core.IFunction;

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

    public ReferenceSector getSector() {
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
    public E get() {
        return (E) sector.hz_get(id);
    }

    @Override
    public Future<E> asyncGet() {
        return (Future<E>) sector.hz_asyncGet(id);
    }

    @Override
    public boolean isNull() {
        return sector.hz_isNull(id);
    }

    @Override
    public Future<Boolean> asyncIsNull() {
        return sector.hz_asyncIsNull(id);
    }

    @Override
    public void set(E update) {
        sector.hz_set(id, update);
    }

    @Override
    public Future<Void> asyncSet(E update) {
        return sector.hz_asyncSet(id, update);
    }

    @Override
    public boolean compareAndSet(E expect, E update) {
        return sector.hz_compareAndSet(id, expect, update);
    }

    @Override
    public Future<Boolean> asyncCompareAndSet(E expect, E update) {
        return sector.hz_asyncCompareAndSet(id, expect, update);
    }

    @Override
    public <R> R apply(IFunction<E, R> f) {
        return (R)sector.hz_apply(id,f);
    }

    @Override
    public <R> Future<R> asyncApply(IFunction<E, R> f) {
        return (Future<R>)sector.hz_asyncApply(id,f);
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
        return "IAtomicReference{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
