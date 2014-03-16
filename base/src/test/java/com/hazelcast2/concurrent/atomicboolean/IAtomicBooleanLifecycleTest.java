package com.hazelcast2.concurrent.atomicboolean;

import com.hazelcast2.core.Hazelcast;
import com.hazelcast2.core.HazelcastInstance;
import com.hazelcast2.core.IdNotFoundException;
import com.hazelcast2.test.HazelcastTestSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class IAtomicBooleanLifecycleTest extends HazelcastTestSupport {

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
    public void construction_withConfigAndNullName(){
        AtomicBooleanConfig config = new AtomicBooleanConfig();
        config.asyncBackupCount=1;
        config.backupCount=0;

        IAtomicBoolean atomicBoolean = hz.getAtomicBoolean(config);
        assertNotNull(config.name);

        IAtomicBoolean found = hz.getAtomicBoolean(config.name);
        assertEquals(atomicBoolean.getId(), found.getId());
    }

    @Test
    public void destroy_whenNotDestroyed() {
        IAtomicBoolean ref = hz.getAtomicBoolean(randomString());
        ref.destroy();
        assertTrue(ref.isDestroyed());
    }

    @Test
    public void destroy_whenAlreadyDestroyed() {
        IAtomicBoolean ref = hz.getAtomicBoolean(randomString());
        ref.destroy();

        ref.destroy();

        assertTrue(ref.isDestroyed());
    }

    @Test
    public void isDestroyed_whenNotDestroyed() {
        IAtomicBoolean ref = hz.getAtomicBoolean(randomString());
        assertFalse(ref.isDestroyed());
    }

    @Test
    public void getAtomicBoolean_duplicateRetrieval(){
        String  name = randomString();
        IAtomicBoolean boolean1 = hz.getAtomicBoolean(name);
        boolean1.set(true);
        IAtomicBoolean boolean2 = hz.getAtomicBoolean(name);
        assertTrue(boolean2.get());
    }

    @Test(expected = IdNotFoundException.class)
    public void usageAfterDestroyed(){
        IAtomicBoolean atomicBoolean = hz.getAtomicBoolean(randomString());
        atomicBoolean.destroy();

        atomicBoolean.get();
    }
}
