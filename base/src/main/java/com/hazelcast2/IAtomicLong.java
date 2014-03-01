package com.hazelcast2;

import java.util.concurrent.Future;

public interface IAtomicLong {

    long get();

    Future<Long> asyncGet();

    void set(long update);

    Future<Void> asyncSet(long update);

    void inc();

    Future<Void> asyncInc();

    long getId();
}
