package com.hazelcast2.test;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class HazelcastTestSupport {

    private static final int ASSERT_TRUE_EVENTUALLY_TIMEOUT;

    static {
        System.setProperty("hazelcast.repmap.hooks.allowed", "true");
        ASSERT_TRUE_EVENTUALLY_TIMEOUT = Integer.parseInt(System.getProperty("hazelcast.assertTrueEventually.timeout", "120"));
        System.out.println("ASSERT_TRUE_EVENTUALLY_TIMEOUT = " + ASSERT_TRUE_EVENTUALLY_TIMEOUT);
    }

    public static void assertJoinable(Thread... threads) {
        assertJoinable(ASSERT_TRUE_EVENTUALLY_TIMEOUT, threads);
    }

    public static void assertIterableEquals(Iterable iter, Object... values) {
        int counter = 0;
        for (Object o : iter) {
            if (values.length < counter + 1) {
                throw new AssertionError("Iterator and values sizes are not equal");
            }
            assertEquals(values[counter], o);
            counter++;
        }

        assertEquals("Iterator and values sizes are not equal", values.length, counter);
    }

    public static void assertSizeEventually(int expectedSize, Collection c) {
        assertSizeEventually(expectedSize, c, ASSERT_TRUE_EVENTUALLY_TIMEOUT);
    }

    public static void assertSizeEventually(final int expectedSize, final Collection c, long timeoutSeconds) {
        assertTrueEventually(new AssertTask() {
            @Override
            public void run() {
                assertEquals("the size of the collection is correct", expectedSize, c.size());
            }
        }, timeoutSeconds);
    }

    public static void assertJoinable(long timeoutSeconds, Thread... threads) {
        try {
            long remainingTimeoutMs = TimeUnit.SECONDS.toMillis(timeoutSeconds);
            for (Thread t : threads) {
                long startMs = System.currentTimeMillis();
                t.join(remainingTimeoutMs);

                if (t.isAlive()) {
                    fail("Timeout waiting for thread " + t.getName() + " to terminate");
                }

                long durationMs = System.currentTimeMillis() - startMs;
                remainingTimeoutMs -= durationMs;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void assertOpenEventually(CountDownLatch latch) {
        assertOpenEventually(latch, ASSERT_TRUE_EVENTUALLY_TIMEOUT);
    }

    public static void assertOpenEventually(CountDownLatch latch, long timeoutSeconds) {
        try {
            boolean completed = latch.await(timeoutSeconds, TimeUnit.SECONDS);
            assertTrue("CountDownLatch failed to complete within " + timeoutSeconds + " seconds , count left:" + latch.getCount(), completed);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void sleepSeconds(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
        }
    }

    public static String randomString() {
        return UUID.randomUUID().toString();
    }


    public static void sleepMillis(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
        }
    }

    public static void assertTrueAllTheTime(AssertTask task, long durationSeconds) {
        for (int k = 0; k < durationSeconds; k++) {
            try {
                task.run();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            sleepSeconds(1);
        }
    }

    public static void assertTrueEventually(AssertTask task, long timeoutSeconds) {
        AssertionError error = null;

        //we are going to check 5 times a second.
        long iterations = timeoutSeconds * 5;
        int sleepMillis = 200;
        for (int k = 0; k < iterations; k++) {
            try {
                try {
                    task.run();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return;
            } catch (AssertionError e) {
                error = e;
            }
            sleepMillis(sleepMillis);
        }

        printAllStackTraces();
        throw error;
    }

    public static void assertTrueEventually(AssertTask task) {
        assertTrueEventually(task, ASSERT_TRUE_EVENTUALLY_TIMEOUT);
    }

    public static void printAllStackTraces() {
        Map liveThreads = Thread.getAllStackTraces();
        for (Object o : liveThreads.keySet()) {
            Thread key = (Thread) o;
            System.err.println("Thread " + key.getName());
            StackTraceElement[] trace = (StackTraceElement[]) liveThreads.get(key);
            for (StackTraceElement aTrace : trace) {
                System.err.println("\tat " + aTrace);
            }
        }
    }


}
