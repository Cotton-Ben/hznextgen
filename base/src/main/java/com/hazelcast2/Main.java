package com.hazelcast2;

import com.hazelcast2.core.Hazelcast;
import com.hazelcast2.core.HazelcastInstance;
import com.hazelcast2.core.IAtomicLong;
import com.hazelcast2.instance.HazelcastInstanceImpl;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Main {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        HazelcastInstance hz1 = Hazelcast.newHazelcastInstance();
        HazelcastInstance hz2 = Hazelcast.newHazelcastInstance();
        hz1.startAndJoin(hz2);

        IAtomicLong ref = hz1.getAtomicLong("foo");
        ref.set(10);
        System.out.println(ref.get());

        IAtomicLong ref2 = hz2.getAtomicLong("foo");

        ref2.set(10);

        long startMs = System.currentTimeMillis();
        int iterations = 1000 * 1000 * 1000;
        List<Future> futures = new LinkedList<>();

        for (int k = 0; k < iterations; k++) {
            futures.add(ref2.asyncSet(20));

            if(futures.size()==50){
                for(Future f: futures){
                    f.get();
                }
                futures.clear();
            }

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
