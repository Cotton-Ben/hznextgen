package com.hazelcast2.spi;

import com.hazelcast2.internal.util.InvocationFuture;

public class BlockingInvocationWaitPolicy implements InvocationWaitPolicy {

    @Override
    public void await(InvocationFuture f) throws InterruptedException {
        f.await();
    }
}
