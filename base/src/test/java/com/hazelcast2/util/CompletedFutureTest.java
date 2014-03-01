package com.hazelcast2.util;

import org.junit.Test;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertNull;

public class CompletedFutureTest {

    @Test
    public void testVoidFuture() throws ExecutionException, InterruptedException {
        CompletedFuture future = CompletedFuture.COMPLETED_VOID_FUTURE;
        Object result = future.get();
        assertNull(result);
    }
}
