package com.hazelcast2.map;

import com.hazelcast2.core.Hazelcast;
import com.hazelcast2.core.HazelcastInstance;
import com.hazelcast2.core.IAtomicLong;
import com.hazelcast2.core.IMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

//very naive performance test
public class IMapPerformanceTest {

    private HazelcastInstance hz;

    @Before
    public void setUp() {
        hz = Hazelcast.newHazelcastInstance();
    }

    @After
    public void tearDown(){
        hz.shutdown();
    }

    @Test
    public void testSet() {
        IMap map = hz.getMap("foo");
        long startMs = System.currentTimeMillis();
        int iterations = 1000 * 1000 * 100;
        for (int k = 0; k < iterations; k++) {
            map.set("1","2");
        }
        long durationMs = System.currentTimeMillis() - startMs;
        double performance = (iterations * 1000d) / durationMs;
        System.out.println("Performance: " + performance);
    }

    @Test
    public void testGet() {
        IMap map = hz.getMap("foo");
        map.set("foo","bar");
        int iterations = 1000 * 1000 * 100;
        long startMs = System.currentTimeMillis();

        for (int k = 0; k < iterations; k++) {
            map.get("foo");
        }
        long durationMs = System.currentTimeMillis() - startMs;
        double performance = (iterations * 1000d) / durationMs;
        System.out.println("Performance: " + performance);
    }
}
