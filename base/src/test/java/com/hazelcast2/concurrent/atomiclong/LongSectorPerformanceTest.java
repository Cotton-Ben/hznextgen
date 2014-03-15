package com.hazelcast2.concurrent.atomiclong;

import com.hazelcast2.concurrent.atomiclong.impl.GeneratedLongSector;
import com.hazelcast2.concurrent.atomiclong.impl.LongSector;
import com.hazelcast2.concurrent.atomiclong.impl.LongSectorSettings;
import com.hazelcast2.spi.SectorScheduler;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

//very naive performance test
public class LongSectorPerformanceTest {

    @Test
    public void testSet() {
        LongSector sector = createLongSector();
        long id = sector.createCell(new AtomicLongConfig());
        long startMs = System.currentTimeMillis();
        int iterations  = 1000 * 1000 * 100;
        for (int k = 0; k < iterations; k++) {
            sector.doSet(id, 20);
        }
        long durationMs = System.currentTimeMillis()-startMs;
        double performance = (iterations*1000d)/durationMs;
        System.out.println("Performance: "+performance);
        long result = sector.doGet(id);
        assertEquals(20, result);
    }

    private LongSector createLongSector() {
        SectorScheduler sectorScheduler = new SectorScheduler(1024,1);
        LongSectorSettings settings = new LongSectorSettings();
        settings.partitionId = 1;
        settings.scheduler = sectorScheduler;
        LongSector sector = new GeneratedLongSector(settings);
        sector.unlock();
        return sector;
    }

    @Test
    public void testInc() {
        LongSector longPartition = createLongSector();
        long id = longPartition.createCell(new AtomicLongConfig());
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
        LongSector longPartition = createLongSector();
        long id = longPartition.createCell(new AtomicLongConfig());
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
