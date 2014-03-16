package com.hazelcast2.concurrent.atomiclong;


import com.hazelcast2.core.Hazelcast;
import com.hazelcast2.core.HazelcastInstance;
import org.openjdk.jmh.annotations.*;

@State(value = Scope.Thread)
public class UncontendedAtomicLongBenchmark {

    private HazelcastInstance hz;
    private IAtomicLong atomicLong;

    @Setup
    public void setUp() {
        hz = Hazelcast.newHazelcastInstance();
        hz.startMaster();

        atomicLong = hz.getAtomicLong("foo");
    }

    @TearDown
    public void tearDown() {
        hz.shutdown();
    }

    @GenerateMicroBenchmark
    @OperationsPerInvocation(100000000)
    public void getPerformance() {
        for (int k = 0; k < 100000000; k++) {
            atomicLong.set(k);
        }
    }

    @GenerateMicroBenchmark
    @OperationsPerInvocation(100000000)
    public void setPerformance() {
        for (int k = 0; k < 100000000; k++) {
            atomicLong.get();
        }
    }

    @GenerateMicroBenchmark
    @OperationsPerInvocation(100000000)
    public void incPerformance() {
        for (int k = 0; k < 100000000; k++) {
            atomicLong.inc();
        }
    }
}
