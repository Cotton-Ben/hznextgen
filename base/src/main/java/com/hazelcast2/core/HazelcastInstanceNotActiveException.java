package com.hazelcast2.core;

public class HazelcastInstanceNotActiveException extends IllegalStateException {

    public HazelcastInstanceNotActiveException() {
        super("Hazelcast instance is not active!");
    }
}
