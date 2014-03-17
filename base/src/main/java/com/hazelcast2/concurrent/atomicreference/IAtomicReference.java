package com.hazelcast2.concurrent.atomicreference;

import com.hazelcast2.core.DistributedObject;
import com.hazelcast2.core.IFunction;

import java.util.concurrent.Future;

public interface IAtomicReference<E> extends DistributedObject {

    E get();

    boolean isNull();

    Future<Boolean> asyncIsNull();

    Future<E> asyncGet();

    void set(E update);

    Future<Void> asyncSet(E update);

    boolean compareAndSet(E expect, E update);

    Future<Boolean> asyncCompareAndSet(E expect, E update);

    <R> R apply(IFunction<E,R> f);

    <R> Future<R> asyncApply(IFunction<E,R> f);

    void alter(IFunction<E,E> f);

    Future<Void> asyncAlter(IFunction<E,E> f);
}
