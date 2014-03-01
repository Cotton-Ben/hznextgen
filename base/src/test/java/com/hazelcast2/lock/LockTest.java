package com.hazelcast2.lock;

import com.hazelcast2.core.ILock;
import com.hazelcast2.spi.PartitionSettings;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class LockTest {

    private LockPartition partition;
    private LockCell cell;
    private ILock lock;

    @Before
    public void setUp() {
        partition = new GeneratedLockPartition(new PartitionSettings(1));
        lock = new ILockProxy(partition);
        cell = partition.loadCell(lock.getId());
    }

    @Test
    public void tryLock_whenFree() {
        boolean result = lock.tryLock();
        assertTrue(result);
        assertTrue(lock.isLocked());
    }

    @Test
    public void tryLock_whenLockedByOther() {
        lockByOther(lock);

        boolean result = lock.tryLock();
        assertFalse(result);
        assertTrue(lock.isLocked());
    }

    @Test(expected = IllegalMonitorStateException.class)
    public void unlock_whenUnlocked() {
        lock.unlock();
    }

    @Test
    public void unlock_whenLockedBySelf() {
        lock.tryLock();

        lock.unlock();
        assertFalse(lock.isLocked());
    }

    @Test
    public void unlock_whenLockedByOther() {
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
