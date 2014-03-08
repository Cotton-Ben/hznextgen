package com.hazelcast2.spi;

import com.hazelcast2.util.InvocationFuture;

public class SpinningInvocationWaitPolicy implements InvocationWaitPolicy {

    @Override
    public void await(InvocationFuture f) {
        for (; ; ) {
            if (f.isDone()) {
                return;
            }
        }
    }
}
