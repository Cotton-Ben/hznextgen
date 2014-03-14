package com.hazelcast2.concurrent.atomiclong;

import com.hazelcast2.core.Hazelcast;
import com.hazelcast2.core.HazelcastInstance;
import com.hazelcast2.core.IAtomicLong;
import com.hazelcast2.test.HazelcastTestSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

/**
 * todo:
 * this test can be improved by not immediately doing a future.get but overloading the system with
 * asynchronous calls. But that will only work if we provide back-pressure.
 */
public class IncrementStressTest extends HazelcastTestSupport {

    private HazelcastInstance hz;

    @Before
    public void setUp() {
        hz = Hazelcast.newHazelcastInstance();
        hz.startMaster();
    }

    @After
    public void tearDown() {
        hz.shutdown();
    }

    @Test
    public void testSingleThreadAsync() throws InterruptedException {
        testSingleThread(true, 1000000);
    }

    @Test
    public void testSingleThreadSync() throws InterruptedException {
        testSingleThread(false, 100000000);
    }

    public void testSingleThread(boolean async, int iterations) throws InterruptedException {
        IAtomicLong atomicLong = hz.getAtomicLong(randomString());
        IncThread thread = new IncThread(atomicLong, iterations, async);
        thread.start();
        thread.join();

        assertEquals(iterations, atomicLong.get());
    }

    @Test
    public void testMultipleThreadAsync() throws InterruptedException {
        testMultipleThreads(true,1000000);
    }

    @Test
    public void testMultipleThreadSync() throws InterruptedException {
        testMultipleThreads(false,100000000);
    }

    public void testMultipleThreads(boolean async, int iterations) throws InterruptedException {
        IAtomicLong atomicLong = hz.getAtomicLong(randomString());
        IncThread thread1 = new IncThread(atomicLong, iterations, async);
        IncThread thread2 = new IncThread(atomicLong, iterations, async);
        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();
        assertEquals(2 * iterations, atomicLong.get());
    }

    class IncThread extends Thread {
        private final IAtomicLong atomicLong;
        private final int iterations;
        private final boolean async;

        IncThread(IAtomicLong atomicLong, int iterations, boolean async) {
            this.atomicLong = atomicLong;
            this.iterations = iterations;
            this.async = async;
        }

        public void run() {
            try {
                for (int k = 0; k < iterations; k++) {
                    if (async) {
                        atomicLong.asyncInc().get();
                    } else {
                        atomicLong.inc();
                    }
                    if (k % 100000 == 0) {
                        System.out.println(getName() + " is at: " + k);
                    }
                }
            } catch (RuntimeException | InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }
}
