package com.hazelcast2.atomicboolean;

import java.util.concurrent.Future;

public interface IAtomicBoolean {

    boolean get();

    Future<Boolean> asyncGet();

    boolean set(boolean update);

    Future<Boolean> asyncSet(boolean update);

    boolean compareAndSet(boolean old, boolean update);

    Future<Boolean> asyncCompareAndSet(boolean old, boolean update);
}
