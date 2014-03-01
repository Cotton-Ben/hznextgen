package com.hazelcast2.concurrent.atomiclong;

import com.hazelcast2.core.IAtomicLong;
import com.hazelcast2.spi.PartitionSettings;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

//very naive performance test
public class AtomicLongPerformanceTest {

    @Test
    public void testSet() {
        LongPartition partition = new GeneratedLongPartition(new PartitionSettings(1));
        IAtomicLong atomicLong = new AtomicLongProxy(partition, "foo", 1);
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
        LongPartition partition = new GeneratedLongPartition(new PartitionSettings(1));
        IAtomicLong atomicLong = new AtomicLongProxy(partition, "foo", 1);
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
        LongPartition partition = new GeneratedLongPartition(new PartitionSettings(1));
        IAtomicLong atomicLong = new AtomicLongProxy(partition, "foo", 1);
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
