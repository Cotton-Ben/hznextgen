package com.hazelcast2.core;

import com.hazelcast2.internal.instance.HazelcastInstanceImpl;

public class Hazelcast {

    public static HazelcastInstance newHazelcastInstance(Config config){
        if(config == null){
            config = new Config() ;
        }

        return new HazelcastInstanceImpl(config);
    }

    public static HazelcastInstance newHazelcastInstance() {
        return newHazelcastInstance(new Config());
    }
}
