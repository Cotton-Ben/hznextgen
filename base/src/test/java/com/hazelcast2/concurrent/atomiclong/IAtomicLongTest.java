package com.hazelcast2.concurrent.atomiclong;

import com.hazelcast2.core.Hazelcast;
import com.hazelcast2.core.HazelcastInstance;
import com.hazelcast2.core.LongFunction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;

public class IAtomicLongTest {

    private HazelcastInstance hz;

    @Before
    public void setUp() {
        hz = Hazelcast.newHazelcastInstance();
        hz.startMaster();
    }

    @After
    public void tearDown(){
        hz.shutdown();
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

        Future<Void> voidFuture = atomicLong.asyncSet(20);
        voidFuture.get();

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

    @Test
    public void apply() {
        IAtomicLong atomicLong = hz.getAtomicLong("foo");
        long result = atomicLong.apply(new LongFunction() {
            @Override
            public long apply(long arg) {
                return arg+1;
            }
        });

        assertEquals(0l, atomicLong.get());
        assertEquals(1L, result);
    }
}
