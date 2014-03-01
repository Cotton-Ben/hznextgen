package com.hazelcast2.atomiclong;

import com.hazelcast2.spi.PartitionSettings;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LongPartitionTest {

    @Test
    public void get() {
        LongPartition longPartition = new GeneratedLongPartition(new PartitionSettings(1,1));
        long id = longPartition.createCell();
        long result = longPartition.doGet(id);
        assertEquals(0, result);
    }


    @Test
    public void set() {
        LongPartition longPartition = new GeneratedLongPartition(new PartitionSettings(1,1));
        long id = longPartition.createCell();
        longPartition.doSet(id, 20);
        long result = longPartition.doGet(id);
        assertEquals(20, result);
    }
}
