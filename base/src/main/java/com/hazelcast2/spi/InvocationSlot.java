package com.hazelcast2.spi;

import com.hazelcast2.util.InvocationFuture;

public final class InvocationSlot {

    public volatile long sequence = -1;

    public long id;
    public short functionId;
    public long long1;
    public long long2;
    public long long3;
    public Object reference1;
    public Object reference2;
    public InvocationFuture invocationFuture;

    //for remote calls. Bytes contains the call content. The source so you can do a return.
    public byte[] bytes;
    public InvocationEndpoint source;

    public final void clear() {
        //todo: is doing a null check if fields are not null cheaper?
        reference1 = null;
        reference2 = null;
        invocationFuture = null;
        bytes = null;
        source = null;
    }

    public final void publish(final long sequence) {
        this.sequence = sequence;
    }

    public final void awaitPublication(final long sequence) {
        while (this.sequence != sequence) {
        }
    }
}
