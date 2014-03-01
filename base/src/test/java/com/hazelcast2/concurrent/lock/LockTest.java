package com.hazelcast2.concurrent.lock;

import com.hazelcast2.core.ILock;
import com.hazelcast2.instance.HazelcastInstanceImpl;
import com.hazelcast2.spi.PartitionSettings;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class LockTest {

    private HazelcastInstanceImpl hz;

    @Before
    public void setUp() {
        hz = new HazelcastInstanceImpl();
    }

    @Test
    public void tryLock_whenFree() {
        ILock lock = hz.getLock("foo");

        boolean result = lock.tryLock();
        assertTrue(result);
        assertTrue(lock.isLocked());
    }

    @Test
    public void tryLock_whenLockedByOther() {
        ILock lock = hz.getLock("foo");
        lockByOther(lock);

        boolean result = lock.tryLock();
        assertFalse(result);
        assertTrue(lock.isLocked());
    }

    @Test(expected = IllegalMonitorStateException.class)
    public void unlock_whenUnlocked() {
        ILock lock = hz.getLock("foo");

        lock.unlock();
    }

    @Test
    public void unlock_whenLockedBySelf() {
        ILock lock = hz.getLock("foo");

        lock.tryLock();

        lock.unlock();
        assertFalse(lock.isLocked());
    }

    @Test
    public void unlock_whenLockedByOther() {
        ILock lock = hz.getLock("foo");

        lockByOther(lock);

        try {
            lock.unlock();
            fail();
        } catch (IllegalMonitorStateException e) {

        }

        assertTrue(lock.isLocked());
    }

    public void lockByOther(final ILock lock) {
        Thread thread = new Thread() {
            public void run() {
                lock.tryLock();
            }
        };
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
