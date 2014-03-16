package com.hazelcast2.concurrent.atomicreference;

import com.hazelcast2.core.Hazelcast;
import com.hazelcast2.core.HazelcastInstance;
import com.hazelcast2.core.IdNotFoundException;
import com.hazelcast2.test.HazelcastTestSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class IAtomicReferenceLifecycleTest extends HazelcastTestSupport {
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
        AtomicReferenceConfig config = new AtomicReferenceConfig();
        config.asyncBackupCount=1;
        config.backupCount=0;

        IAtomicReference atomicReference = hz.getAtomicReference(config);
        assertNotNull(config.name);

        IAtomicReference found = hz.getAtomicReference(config.name);
        assertEquals(atomicReference.getId(), found.getId());
    }

    @Test
    public void getAtomicReference_duplicateRetrieval(){
        String  name = randomString();
        IAtomicReference<String> ref1 = hz.getAtomicReference(name);
        ref1.set("foo");
        IAtomicReference ref2 = hz.getAtomicReference(name);
        assertEquals("foo",ref2.get());
    }

    @Test
    public void destroy_whenNotDestroyed() {
        IAtomicReference ref = hz.getAtomicReference(randomString());
        ref.destroy();
        assertTrue(ref.isDestroyed());
    }

    @Test
    public void destroy_whenAlreadyDestroyed() {
        IAtomicReference ref = hz.getAtomicReference(randomString());
        ref.destroy();

        ref.destroy();

        assertTrue(ref.isDestroyed());
    }

    @Test
    public void isDestroyed_whenNotDestroyed() {
        IAtomicReference ref = hz.getAtomicReference(randomString());
        assertFalse(ref.isDestroyed());
    }

    @Test(expected = IdNotFoundException.class)
    public void usageAfterDestroyed(){
        IAtomicReference atomicReference = hz.getAtomicReference(randomString());
        atomicReference.destroy();

        atomicReference.get();
    }

}
