package com.hazelcast2.spi;

import com.hazelcast2.util.InvocationFuture;

public interface InvocationWaitPolicy {

    void await(InvocationFuture f) throws InterruptedException;
}
