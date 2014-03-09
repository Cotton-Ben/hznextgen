package com.hazelcast2.concurrent.atomicreference;

import com.hazelcast2.core.Hazelcast;
import com.hazelcast2.core.HazelcastInstance;
import com.hazelcast2.core.IAtomicReference;
import com.hazelcast2.test.HazelcastTestSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.Assert.*;

public class IAtomicReferenceTest extends HazelcastTestSupport {
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
        IAtomicReference<String> atomicReference = hz.getAtomicReference(randomString());

        String result = atomicReference.get();

        assertNull(result);
    }

    @Test
    public void asyncGet() throws ExecutionException, InterruptedException {
        IAtomicReference<String> atomicReference = hz.getAtomicReference(randomString());
        String update = "bar";
        atomicReference.set(update);

        String result = atomicReference.asyncGet().get();

        assertEquals(update, result);
    }

    @Test
    public void set() {
        IAtomicReference<String> atomicReference = hz.getAtomicReference(randomString());

        String update = "bar";
        atomicReference.set(update);

        assertEquals(update, atomicReference.get());
    }

    @Test
    public void asyncSet() throws ExecutionException, InterruptedException {
        IAtomicReference<String> atomicReference = hz.getAtomicReference(randomString());

        String update = "bar";

        Future<Void> voidFuture = atomicReference.asyncSet(update);
        voidFuture.get();

        assertEquals(update, atomicReference.get());
    }

    @Test
    public void compareAndSet() throws ExecutionException, InterruptedException {
        IAtomicReference<String> atomicReference = hz.getAtomicReference(randomString());
        String update = "bar";

        boolean result = atomicReference.compareAndSet(null, update);

        assertTrue(result);
        assertEquals(update, atomicReference.get());
    }

    @Test
    public void asyncCompareAndSet() throws ExecutionException, InterruptedException {
        IAtomicReference<String> atomicReference = hz.getAtomicReference("foo");
        String update = "bar";

        boolean result = atomicReference.asyncCompareAndSet(null, update).get();

        assertTrue(result);
        assertEquals(update, atomicReference.get());
    }

}
