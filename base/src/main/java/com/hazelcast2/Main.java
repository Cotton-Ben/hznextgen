package com.hazelcast2;

import com.hazelcast2.core.Hazelcast;
import com.hazelcast2.core.HazelcastInstance;
import com.hazelcast2.core.IAtomicLong;
import com.hazelcast2.instance.HazelcastInstanceImpl;

public class Main {

    public static void main(String[] args){
        HazelcastInstance hz1 = Hazelcast.newHazelcastInstance();
        HazelcastInstance hz2 = Hazelcast.newHazelcastInstance();
        hz1.startAndJoin(hz2);

        IAtomicLong ref = hz1.getAtomicLong("foo");
        ref.set(10);
        System.out.println(ref.get());

        IAtomicLong ref2 = hz2.getAtomicLong("foo");

        ref2.set(10);

        long startMs = System.currentTimeMillis();
        int iterations = 1000 * 1000 * 10;
        for (int k = 0; k < iterations; k++) {
            ref2.set(20);
            if(k % 100000 == 0){
                System.out.println("At k:"+k);
            }
        }
        long durationMs = System.currentTimeMillis() - startMs;
        double performance = (iterations * 1000d) / durationMs;
        System.out.println("Performance: " + performance);


        System.out.println(ref2.get());
    }

    public int getAnyPartitionOwnedBy(HazelcastInstanceImpl hz){
        for(int k=0;k<271;k++){
            if(hz.ownsPartition(k)){
                return k;
            }
        }

        throw new IllegalStateException();
    }
}
