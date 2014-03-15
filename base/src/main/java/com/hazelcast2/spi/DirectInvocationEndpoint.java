package com.hazelcast2.spi;

import com.hazelcast2.internal.instance.HazelcastInstanceImpl;

public class DirectInvocationEndpoint implements InvocationEndpoint {

    private final HazelcastInstanceImpl hazelcastInstance;
    public InvocationEndpoint source;

    public DirectInvocationEndpoint(HazelcastInstanceImpl hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

    @Override
    public void send(byte[] bytes) {
        hazelcastInstance.dispatch(source, bytes);
    }
}
