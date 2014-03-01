package com.hazelcast2.util;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class CompletedFuture<E> implements Future<E> {

    public static final CompletedFuture COMPLETED_VOID_FUTURE = new CompletedFuture(null);

    private final Object result;

    public CompletedFuture(Object result) {
        this.result = result;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return true;
    }

    @Override
    public E get() {
        return null;
    }

    @Override
    public E get(long timeout, TimeUnit unit) {
        return null;
    }
}
