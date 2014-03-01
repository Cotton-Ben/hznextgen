package com.hazelcast2.spi;

import com.hazelcast2.InvocationFuture;

public interface InvocationWaitPolicy {

    void await(InvocationFuture f) throws InterruptedException;
}
