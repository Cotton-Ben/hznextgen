package com.hazelcast2.concurrent.atomiclong;

import com.hazelcast2.core.Hazelcast;
import com.hazelcast2.core.HazelcastInstance;
import com.hazelcast2.test.HazelcastTestSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

//very naive performance test
public class AtomicLongPerformanceTest extends HazelcastTestSupport{

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
    public void testSet() {
        IAtomicLong atomicLong = hz.getAtomicLong(randomString());
        long startMs = System.currentTimeMillis();
        int iterations = 1000 * 1000 * 100;
        for (int k = 0; k < iterations; k++) {
            atomicLong.set(20);
        }
        long durationMs = System.currentTimeMillis() - startMs;
        double performance = (iterations * 1000d) / durationMs;
        System.out.println("Performance: " + performance);
        long result = atomicLong.get();
        assertEquals(20, result);
    }

    @Test
    public void testInc() {
        IAtomicLong atomicLong = hz.getAtomicLong(randomString());
        int iterations = 1000 * 1000 * 100;
        long startMs = System.currentTimeMillis();
        for (int k = 0; k < iterations; k++) {
            atomicLong.inc();
        }
        long durationMs = System.currentTimeMillis() - startMs;
        double performance = (iterations * 1000d) / durationMs;
        System.out.println("Performance: " + performance);
        long result = atomicLong.get();
        assertEquals(iterations, result);
    }

    @Test
    public void testGet() {
        IAtomicLong atomicLong = hz.getAtomicLong(randomString());
        int iterations = 1000 * 1000 * 100;
        long startMs = System.currentTimeMillis();
        for (int k = 0; k < iterations; k++) {
            atomicLong.get();
        }
        long durationMs = System.currentTimeMillis() - startMs;
        double performance = (iterations * 1000d) / durationMs;
        System.out.println("Performance: " + performance);
    }
}
