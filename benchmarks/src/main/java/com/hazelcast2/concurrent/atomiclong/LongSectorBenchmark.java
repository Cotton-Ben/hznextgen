package com.hazelcast2.concurrent.atomiclong;

import com.hazelcast2.concurrent.atomiclong.impl.GeneratedLongSector;
import com.hazelcast2.concurrent.atomiclong.impl.LongSector;
import com.hazelcast2.concurrent.atomiclong.impl.LongSectorSettings;
import com.hazelcast2.spi.SectorScheduler;
import org.openjdk.jmh.annotations.GenerateMicroBenchmark;
import org.openjdk.jmh.annotations.OperationsPerInvocation;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

@State(value = Scope.Thread)
public class LongSectorBenchmark {

    @GenerateMicroBenchmark
    @OperationsPerInvocation(100000000)
    public void testSet() {
        LongSector sector = createLongSector();
        long id = sector.createCell(new AtomicLongConfig());
        int iterations = 1000 * 1000 * 100;
        for (int k = 0; k < iterations; k++) {
            sector.doSet(id, 20);
        }
    }

    private LongSector createLongSector() {
        SectorScheduler sectorScheduler = new SectorScheduler(1024, 1);
        LongSectorSettings settings = new LongSectorSettings();
        settings.partitionId = 1;
        settings.scheduler = sectorScheduler;
        LongSector sector = new GeneratedLongSector(settings);
        sector.unlock();
        return sector;
    }

    @GenerateMicroBenchmark
    @OperationsPerInvocation(100000000)
    public void testInc() {
        LongSector longPartition = createLongSector();
        long id = longPartition.createCell(new AtomicLongConfig());
        int iterations = 1000 * 1000 * 100;
        for (int k = 0; k < iterations; k++) {
            longPartition.doInc(id);
        }
    }

    @GenerateMicroBenchmark
    @OperationsPerInvocation(100000000)
    public void testGet() {
        LongSector longPartition = createLongSector();
        long id = longPartition.createCell(new AtomicLongConfig());
        int iterations = 1000 * 1000 * 100;
        for (int k = 0; k < iterations; k++) {
            longPartition.doGet(id);
        }
    }
}
