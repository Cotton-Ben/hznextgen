package com.hazelcast2.util;

import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

public class InvocationFutureTest {

    @Test
    @Ignore
    public void test() throws ExecutionException, InterruptedException {
        InvocationFuture f = new InvocationFuture();
        f.getSafely();
    }


}