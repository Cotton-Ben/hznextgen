package com.hazelcast2.util;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class InvocationFuture implements Future {

    public final static Object NO_RESPONSE = new Object() {
        public String toString() {
            return "noResponse";
        }
    };

    public volatile Object value = NO_RESPONSE;

    public void cancel() {
        throw new UnsupportedOperationException();
    }

    public void setResponseException(Throwable t) {
        setResponse(new Failure(t));
    }

    public void setResponse(Object value) {
        synchronized (this) {
            this.value = value;
            notifyAll();
        }
    }

    public void setVoidResponse() {
        setResponse(null);
    }

    public Object getSafely() {
        if (value == NO_RESPONSE) {
            synchronized (this) {
                while (value == NO_RESPONSE) {
                    try {
                        //todo: instead of waiting, try to see if there is work that can be done.
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        if (value instanceof Failure) {
            Throwable throwable = ((Failure) value).t;
            if (throwable instanceof RuntimeException) {
                throw (RuntimeException) throwable;
            }

            if (throwable instanceof Error) {
                throw (Error) throwable;
            }

            throw new RuntimeException(throwable);
        }

        return value;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCancelled() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDone() {
        return value != NO_RESPONSE;
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        //todo: this isn't the right call because we are not dealing correctly with exceptions
        //but for the time being it is good enough.
        return getSafely();
    }

    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        throw new UnsupportedOperationException();
    }

    public void await() throws InterruptedException {
        if (value != NO_RESPONSE) {
            return;
        }

        synchronized (this) {
            while (value == NO_RESPONSE) {
                wait();
            }
        }
    }

    private static class Failure {
        private Throwable t;

        private Failure(Throwable t) {
            this.t = t;
        }
    }
}
