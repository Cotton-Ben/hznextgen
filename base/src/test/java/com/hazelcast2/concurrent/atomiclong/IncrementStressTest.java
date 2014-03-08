package com.hazelcast2.concurrent.atomiclong;

import com.hazelcast2.core.Hazelcast;
import com.hazelcast2.core.HazelcastInstance;
import com.hazelcast2.core.IAtomicLong;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class IncrementStressTest {

    private HazelcastInstance hz;

    @Before
    public void setUp() {
        hz = Hazelcast.newHazelcastInstance();
    }

    @After
    public void tearDown() {
        hz.shutdown();
    }

    @Test
    public void testSingleThread() throws InterruptedException {
        IAtomicLong atomicLong = hz.getAtomicLong("counter");
        int iterations = 100;
        IncThread thread = new IncThread(atomicLong, iterations);
        thread.start();
        thread.join();

        assertEquals(iterations, atomicLong.get());
        //assertNull(cell.invocation);
    }

    @Test
    public void testMultipleThreads() throws InterruptedException {
        int iterations = 100000000;
        IAtomicLong atomicLong = hz.getAtomicLong("counter");
        IncThread thread1 = new IncThread(atomicLong, iterations);
        IncThread thread2 = new IncThread(atomicLong, iterations);
        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();
        assertEquals(2 * iterations, atomicLong.get());
    }

    class IncThread extends Thread {
        private final IAtomicLong atomicLong;
        private final int iterations;

        IncThread(IAtomicLong atomicLong, int iterations) {
            this.atomicLong = atomicLong;
            this.iterations = iterations;
        }

        public void run() {
            try {
                for (int k = 0; k < iterations; k++) {
                    atomicLong.inc();
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
