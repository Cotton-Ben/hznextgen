package com.hazelcast2.core;

import java.util.concurrent.Future;

public interface IAtomicReference<E> extends DistributedObject {

    E get();

    Future<E> asyncGet();

    void set(E update);

    Future<Void> asyncSet(E update);

    boolean compareAndSet(E expect, E update);

    Future<Boolean> asyncCompareAndSet(E expect, E update);
}