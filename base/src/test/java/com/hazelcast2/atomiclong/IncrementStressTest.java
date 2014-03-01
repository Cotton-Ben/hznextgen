package com.hazelcast2.atomiclong;

import com.hazelcast2.spi.PartitionSettings;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class IncrementStressTest {

    @Test
    public void testSingleThread() throws InterruptedException {
        GeneratedLongPartition partition = new GeneratedLongPartition(new PartitionSettings(1,1));
        long address = partition.createCell();
        int iterations = 100;
        IncThread thread = new IncThread(partition, address, iterations);
        thread.start();
        thread.join();

        LongCell cell = partition.loadCell(address);
        assertEquals(cell.value, iterations);
        //assertNull(cell.invocation);
    }

    @Test
    public void testMultipleThreads() throws InterruptedException {
        GeneratedLongPartition partition = new GeneratedLongPartition(new PartitionSettings(1,1));
        long address = partition.createCell();
        int iterations = 100000000;
        IncThread thread1 = new IncThread(partition, address, iterations);
        IncThread thread2 = new IncThread(partition, address, iterations);
        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();
        LongCell cell = partition.loadCell(address);
        assertEquals(2 * iterations,cell.value);
        //assertNull(cell.invocation);
    }

    class IncThread extends Thread {
        private final GeneratedLongPartition longPartition;
        private final long address;
        private final int iterations;

        IncThread(GeneratedLongPartition logic, long address, int iterations) {
            this.longPartition = logic;
            this.address = address;
            this.iterations = iterations;
        }

        public void run() {
            try {
                for (int k = 0; k < iterations; k++) {
                    longPartition.doInc(address);
                    if (k % 10000000 == 0) {
                        System.out.println(getName() + " is at: " + k);
                    }
                }
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
    }
}
