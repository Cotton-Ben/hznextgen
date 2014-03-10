package com.hazelcast2.spi;

import com.hazelcast2.util.InvocationFuture;

public final class Invocation {

    public volatile long sequence = -1;

    public long id;
    public int functionId;
    public long long1;
    public long long2;
    public long long3;
    public Object reference1;
    public Object reference2;
    public InvocationFuture invocationFuture;

    public final void clear() {
        reference1 = null;
        reference2 = null;
        invocationFuture = null;
    }

    public final void publish(final long sequence) {
        this.sequence = sequence;
    }

    public final void awaitPublication(final long sequence) {
        while (this.sequence != sequence) {
        }
    }
}
