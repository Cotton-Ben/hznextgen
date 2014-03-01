package com.hazelcast2;

import org.junit.Test;

import java.util.concurrent.ExecutionException;

/**
 * Created by alarmnummer on 2/20/14.
 */
public class InvocationFutureTest {

    @Test
    public void test() throws ExecutionException, InterruptedException {
        InvocationFuture f = new InvocationFuture();
        f.getSafely();
    }
}
