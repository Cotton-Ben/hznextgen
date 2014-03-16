package com.hazelcast2.concurrent.atomiclong;

import com.hazelcast2.core.Hazelcast;
import com.hazelcast2.core.HazelcastInstance;
import com.hazelcast2.core.IdNotFoundException;
import com.hazelcast2.test.HazelcastTestSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class IAtomicLongLifecycleTest extends HazelcastTestSupport {

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
    public void getAtomicLong_duplicateRetrieval() {
        String name = randomString();
        IAtomicLong long1 = hz.getAtomicLong(name);
        long1.set(20);
        IAtomicLong long2 = hz.getAtomicLong(name);
        assertEquals(20, long2.get());
    }

    @Test
    public void construction_withConfigAndNullName() {
        AtomicLongConfig config = new AtomicLongConfig();
        config.asyncBackupCount = 1;
        config.backupCount = 0;

        IAtomicLong atomicLong = hz.getAtomicLong(config);
        assertNotNull(config.name);

        IAtomicLong found = hz.getAtomicLong(config.name);
        assertEquals(atomicLong.getId(), found.getId());
    }

    @Test
    public void destroy_whenNotDestroyed() {
        IAtomicLong atomicLong = hz.getAtomicLong(randomString());
        atomicLong.destroy();
        assertTrue(atomicLong.isDestroyed());
    }

    @Test
    public void destroy_whenAlreadyDestroyed() {
        IAtomicLong atomicLong = hz.getAtomicLong(randomString());
        atomicLong.destroy();

        atomicLong.destroy();

        assertTrue(atomicLong.isDestroyed());
    }

    @Test
    public void isDestroyed_whenNotDestroyed() {
        IAtomicLong atomicLong = hz.getAtomicLong(randomString());
        assertFalse(atomicLong.isDestroyed());
    }

    @Test(expected = IdNotFoundException.class)
    public void usageAfterDestroyed(){
        IAtomicLong atomicLong = hz.getAtomicLong(randomString());
        atomicLong.destroy();

        atomicLong.set(10);
    }
}
