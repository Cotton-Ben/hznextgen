package com.hazelcast2.concurrent.atomicboolean;

import com.hazelcast2.core.Hazelcast;
import com.hazelcast2.core.HazelcastInstance;
import com.hazelcast2.test.HazelcastTestSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class IAtomicBooleanTest extends HazelcastTestSupport {

    private HazelcastInstance hz;

    @Before
    public void setUp() {
        hz = Hazelcast.newHazelcastInstance();
        hz.startMaster();
    }

    @After
    public void tearDown() {
        hz.shutdown();
    }

    @Test
    public void getAtomicBoolean_duplicateRetrieval(){
        String  name = randomString();
        IAtomicBoolean boolean1 = hz.getAtomicBoolean(name);
        boolean1.set(true);
        IAtomicBoolean boolean2 = hz.getAtomicBoolean(name);
        assertTrue(boolean2.get());
    }

    @Test
    public void get() {
        IAtomicBoolean atomicBoolean = hz.getAtomicBoolean("foo");

        boolean result = atomicBoolean.get();

        assertFalse(result);
   }

    @Test
    public void asyncGet() throws ExecutionException, InterruptedException {
        IAtomicBoolean atomicBoolean = hz.getAtomicBoolean("foo");

        Future<Boolean> voidFuture = atomicBoolean.asyncGet();
        boolean result = voidFuture.get();

        assertFalse(result);
    }


    @Test
    public void set() {
        IAtomicBoolean atomicBoolean = hz.getAtomicBoolean("foo");

        atomicBoolean.set(true);

        assertTrue(atomicBoolean.get());
    }

    @Test
    public void asyncSet() throws ExecutionException, InterruptedException {
        IAtomicBoolean atomicBoolean = hz.getAtomicBoolean("foo");

        Future<Boolean> voidFuture = atomicBoolean.asyncSet(true);
        boolean result = voidFuture.get();

        assertFalse(result);
        assertTrue(atomicBoolean.get());
    }
}
