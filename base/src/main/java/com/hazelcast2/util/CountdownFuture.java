package com.hazelcast2.util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CountdownFuture implements Future {

    private final CountDownLatch countDownLatch;

    public CountdownFuture(CountDownLatch countDownLatch) {
        if (countDownLatch == null) {
            throw new NullPointerException();
        }
        this.countDownLatch = countDownLatch;
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
        return countDownLatch.getCount() == 0;
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        countDownLatch.await();
        return null;
    }

    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        boolean completed = countDownLatch.await(timeout, unit);
        if (!completed) {
            throw new TimeoutException();
        }
        return null;
    }
}
