package com.hazelcast2.map;

import com.hazelcast2.core.Hazelcast;
import com.hazelcast2.core.HazelcastInstance;
import com.hazelcast2.core.IMap;
import com.hazelcast2.test.HazelcastTestSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class IMapTest extends HazelcastTestSupport {

    private HazelcastInstance hz;

    @Before
    public void setUp() {
        hz = Hazelcast.newHazelcastInstance();
    }

    @After
    public void tearDown() {
        hz.shutdown();
    }

    public void get() {
        IMap map = hz.getMap(randomString());
        assertEquals(null, map.get("1"));
    }

    @Test
    public void set() {
        IMap map = hz.getMap(randomString());
        map.set("1", "2");
        assertEquals("2", map.get("1"));
    }
}
