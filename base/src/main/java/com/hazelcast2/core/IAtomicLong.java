package com.hazelcast2.core;

import java.util.concurrent.Future;

public interface IAtomicLong extends DistributedObject {

    long get();

    Future<Long> asyncGet();

    void set(long update);

    Future<Void> asyncSet(long update);

    void inc();

    Future<Void> asyncInc();
}