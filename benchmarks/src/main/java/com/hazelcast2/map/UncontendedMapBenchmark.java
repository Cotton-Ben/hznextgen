package com.hazelcast2.map;

import com.hazelcast2.core.Hazelcast;
import com.hazelcast2.core.HazelcastInstance;
import org.openjdk.jmh.annotations.*;

@State(value = Scope.Thread)
public class UncontendedMapBenchmark {

    private HazelcastInstance hz;

    @Setup
    public void setUp() {
        hz = Hazelcast.newHazelcastInstance();
        hz.startMaster();
    }

    @TearDown
    public void tearDown() {
        hz.shutdown();
    }

    @GenerateMicroBenchmark
    @OperationsPerInvocation(100000000)
    public void testSet() {
        IMap map = hz.getMap("foo");
        long startMs = System.currentTimeMillis();
        int iterations = 1000 * 1000 * 100;
        for (int k = 0; k < iterations; k++) {
            map.set("1", "2");
        }
        long durationMs = System.currentTimeMillis() - startMs;
        double performance = (iterations * 1000d) / durationMs;
        System.out.println("Performance: " + performance);
    }

    @GenerateMicroBenchmark
    @OperationsPerInvocation(100000000)
    public void testGet() {
        IMap map = hz.getMap("foo");
        map.set("foo", "bar");
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
