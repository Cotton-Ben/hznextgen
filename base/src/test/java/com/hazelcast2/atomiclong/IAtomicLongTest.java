package com.hazelcast2.atomiclong;

import com.hazelcast2.core.IAtomicLong;
import com.hazelcast2.spi.PartitionSettings;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

public class IAtomicLongTest {

    private LongPartition partition;
    private LongCell cell;
    private IAtomicLong atomicLong;

    @Before
    public void setUp() {
        partition = new GeneratedLongPartition(new PartitionSettings(1));
        atomicLong = new AtomicLongProxy(partition);
        cell = partition.loadCell(atomicLong.getId());
    }

    @Test
    public void get() {
        cell.value = 10;
        long result = atomicLong.get();
        assertEquals(10L, result);
    }

    @Test
    public void set() {
        atomicLong.set(20);
        assertEquals(20L, atomicLong.get());
    }

    @Test
    public void asyncSet() throws ExecutionException, InterruptedException {
        atomicLong.asyncSet(20).get();
        assertEquals(20L, atomicLong.get());
    }

    @Test
    public void inc() {
        atomicLong.inc();
        assertEquals(1L, atomicLong.get());
    }

    @Test
    public void asyncInc() throws ExecutionException, InterruptedException {
         atomicLong.asyncInc().get();
         assertEquals(1L, atomicLong.get());
    }
}
