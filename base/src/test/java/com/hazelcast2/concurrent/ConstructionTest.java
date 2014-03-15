package com.hazelcast2.concurrent;

import com.hazelcast2.concurrent.atomiclong.IAtomicLong;
import com.hazelcast2.core.Hazelcast;
import com.hazelcast2.core.HazelcastInstance;
import com.hazelcast2.test.HazelcastTestSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConstructionTest extends HazelcastTestSupport {
    private HazelcastInstance hz1;
    private HazelcastInstance hz2;

    @Before
    public void setUp() {
        hz1 = Hazelcast.newHazelcastInstance();
        hz2 = Hazelcast.newHazelcastInstance();
        hz1.startAndJoin(hz2);
    }

    @After
    public void tearDown() {
        hz1.shutdown();
        hz2.shutdown();
    }

    @Test
    public void test(){
        for(int k=0;k<1000;k++){
            String name = randomString();
            IAtomicLong counter1 = hz1.getAtomicLong(name);
            counter1.set(10);
            IAtomicLong counter2 = hz2.getAtomicLong(name);
            assertEquals(10, counter2.get());
        }
    }
}
