package com.hazelcast2.core;

import com.hazelcast2.instance.HazelcastInstanceImpl;

public class Hazelcast {

    public static HazelcastInstance newHazelcastInstance() {
        return new HazelcastInstanceImpl();
    }
}
