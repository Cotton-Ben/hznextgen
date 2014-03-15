package com.hazelcast2.internal.util;

import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

public class InvocationSlotFutureTest {

    @Test
    @Ignore
    public void test() throws ExecutionException, InterruptedException {
        InvocationFuture f = new InvocationFuture();
        f.getSafely();
    }


}
