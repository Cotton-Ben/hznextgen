package com.hazelcast2.concurrent.lock;

import com.hazelcast2.core.Hazelcast;
import com.hazelcast2.core.HazelcastInstance;
import com.hazelcast2.core.IdNotFoundException;
import com.hazelcast2.test.HazelcastTestSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class LockLifecycleTest extends HazelcastTestSupport {
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
    public void construction_withConfigAndNullName() {
        LockConfig config = new LockConfig();
        config.asyncBackupCount = 1;
        config.backupCount = 0;

        ILock lock = hz.getLock(config);
        assertNotNull(config.name);

        ILock found = hz.getLock(config.name);
        assertEquals(lock.getId(), found.getId());
    }

    @Test
    public void getAtomicReference_duplicateRetrieval() {
        String name = randomString();
        ILock lock1 = hz.getLock(name);
        lock1.tryLock();
        ILock lock2 = hz.getLock(name);
        assertTrue(lock2.isLocked());
    }

    @Test
    public void destroy_whenNotDestroyed() {
        ILock lock = hz.getLock(randomString());
        lock.destroy();
        assertTrue(lock.isDestroyed());
    }

    @Test
    public void destroy_whenAlreadyDestroyed() {
        ILock lock = hz.getLock(randomString());
        lock.destroy();

        lock.destroy();

        assertTrue(lock.isDestroyed());
    }

    @Test
    public void isDestroyed_whenNotDestroyed() {
        ILock ref = hz.getLock(randomString());
        assertFalse(ref.isDestroyed());
    }

    @Test(expected = IdNotFoundException.class)
    public void usageAfterDestroyed() {
        ILock atomicReference = hz.getLock(randomString());
        atomicReference.destroy();

        atomicReference.isLocked();
    }
}
