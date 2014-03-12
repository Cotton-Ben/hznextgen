package com.hazelcast2.core;

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
}
