package com.hazelcast2.concurrent.atomiclong;

import com.hazelcast2.core.DistributedObject;
import com.hazelcast2.core.LongFunction;

import java.util.concurrent.Future;

public interface IAtomicLong extends DistributedObject {

    long get();

    Future<Long> asyncGet();

    void set(long update);

    Future<Void> asyncSet(long update);

    void inc();

    Future<Void> asyncInc();

    boolean compareAndSet(long expect, long update);

    Future<Boolean> asyncCompareAndSet(long expect, long update);

    long apply(LongFunction f);

    Future<Long> asyncApply(LongFunction f);

    void alter(LongFunction f);

    Future<Void> asyncAlter(LongFunction f);

    //todo: needs to be moved to distributed object
    void destroy();

    //todo: needs to be moved to distributed object
    boolean isDestroyed();
}
