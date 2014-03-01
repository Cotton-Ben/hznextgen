package com.hazelcast2.atomiclong;

import com.hazelcast2.IAtomicLong;
import com.hazelcast2.spi.PartitionSettings;
import org.junit.Before;
import org.junit.Test;

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
        assertEquals(20L, cell.value);
    }

    @Test
    public void inc() {
        atomicLong.inc();
        assertEquals(1L, cell.value);
    }
}
