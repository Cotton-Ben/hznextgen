package com.hazelcast2.concurrent.atomiclong;

import com.hazelcast2.spi.SectorScheduler;
import com.hazelcast2.spi.PartitionSettings;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

//very naive performance test
public class LongSectorPerformanceTest {

    @Test
    public void testSet() {
        LongSector longPartition = createLongPartition();
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

    private LongSector createLongPartition() {
        SectorScheduler sectorScheduler = new SectorScheduler(1024,1);
        return new GeneratedLongSector(new PartitionSettings(1, sectorScheduler));
    }

    @Test
    public void testInc() {
        LongSector longPartition = createLongPartition();
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
        LongSector longPartition = createLongPartition();
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
