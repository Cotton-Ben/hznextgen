package com.hazelcast2.concurrent.atomiclong;

import com.hazelcast2.spi.PartitionScheduler;
import com.hazelcast2.spi.PartitionSettings;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

//very naive performance test
public class LongPartitionPerformanceTest {

    @Test
    public void testSet() {
        LongPartition longPartition = createLongPartition();
        long id = longPartition.createCell();
        long startMs = System.currentTimeMillis();
        int iterations  = 1000 * 1000 * 100;
        for (int k = 0; k < iterations; k++) {
            longPartition.doSet(id, 20);
        }
        long durationMs = System.currentTimeMillis()-startMs;
        double performance = (iterations*1000d)/durationMs;
        System.out.println("Performance: "+performance);
        long result = longPartition.doGet(id);
        assertEquals(20, result);
    }

    private LongPartition createLongPartition() {
        PartitionScheduler partitionScheduler = new PartitionScheduler(1024);
        return new GeneratedLongPartition(new PartitionSettings(1,partitionScheduler));
    }

    @Test
    public void testInc() {
        LongPartition longPartition = createLongPartition();
        long id = longPartition.createCell();
        int iterations  = 1000 * 1000 * 100;
        long startMs = System.currentTimeMillis();
        for (int k = 0; k < iterations ; k++) {
            longPartition.doInc(id);
        }
        long durationMs = System.currentTimeMillis()-startMs;
        double performance = (iterations*1000d)/durationMs;
        System.out.println("Performance: "+performance);
        long result = longPartition.doGet(id);
        assertEquals(iterations, result);
    }

    @Test
    public void testGet() {
        LongPartition longPartition = createLongPartition();
        long id = longPartition.createCell();
        int iterations  = 1000 * 1000 * 100;
        long startMs = System.currentTimeMillis();
        for (int k = 0; k < iterations ; k++) {
            longPartition.doGet(id);
        }
        long durationMs = System.currentTimeMillis()-startMs;
        double performance = (iterations*1000d)/durationMs;
        System.out.println("Performance: "+performance);
        long result = longPartition.doGet(id);
        assertEquals(0, result);
    }
}
