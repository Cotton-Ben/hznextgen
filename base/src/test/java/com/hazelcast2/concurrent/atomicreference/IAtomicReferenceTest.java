package com.hazelcast2.concurrent.atomicreference;

import com.hazelcast2.core.Hazelcast;
import com.hazelcast2.core.HazelcastInstance;
import com.hazelcast2.core.IAtomicReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class IAtomicReferenceTest {
    private HazelcastInstance hz;

    @Before
    public void setUp() {
        hz = Hazelcast.newHazelcastInstance();
    }

    @After
    public void tearDown() {
        hz.shutdown();
    }

    @Test
    public void get() {
        IAtomicReference<String> atomicReference = hz.getAtomicReference("foo");

        String result = atomicReference.get();

        assertNull(result);
    }

    @Test
    public void asyncGet() throws ExecutionException, InterruptedException {
        IAtomicReference<String> atomicReference = hz.getAtomicReference("foo");
        String update = "bar";
        atomicReference.set(update);

        String result = atomicReference.asyncGet().get();

        assertEquals(update, result);
    }

    @Test
    public void set() {
        IAtomicReference<String> atomicReference = hz.getAtomicReference("foo");

        String update = "bar";
        atomicReference.set(update);

        assertEquals(update, atomicReference.get());
    }

    @Test
    public void asyncSet() throws ExecutionException, InterruptedException {
        IAtomicReference<String> atomicReference = hz.getAtomicReference("foo");

        String update = "bar";

        Future<Void> voidFuture = atomicReference.asyncSet(update);
        voidFuture.get();

        assertEquals(update, atomicReference.get());
    }

}
