package com.hazelcast2.concurrent.atomiclong;

import com.hazelcast2.core.IAtomicLong;
import com.hazelcast2.instance.HazelcastInstanceImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

public class IAtomicLongTest {

    private HazelcastInstanceImpl hz;

    @Before
    public void setUp() {
        hz = new HazelcastInstanceImpl();
    }

    @Test
    public void get() {
        IAtomicLong atomicLong = hz.getAtomicLong("foo");

        long result = atomicLong.get();
        assertEquals(0L, result);
    }

    @Test
    public void set() {
        IAtomicLong atomicLong = hz.getAtomicLong("foo");

        atomicLong.set(20);
        assertEquals(20L, atomicLong.get());
    }

    @Test
    public void asyncSet() throws ExecutionException, InterruptedException {
        IAtomicLong atomicLong = hz.getAtomicLong("foo");

        atomicLong.asyncSet(20).get();
        assertEquals(20L, atomicLong.get());
    }

    @Test
    public void inc() {
        IAtomicLong atomicLong = hz.getAtomicLong("foo");

        atomicLong.inc();
        assertEquals(1L, atomicLong.get());
    }

    @Test
    public void asyncInc() throws ExecutionException, InterruptedException {
        IAtomicLong atomicLong = hz.getAtomicLong("foo");

        atomicLong.asyncInc().get();
        assertEquals(1L, atomicLong.get());
    }
}
