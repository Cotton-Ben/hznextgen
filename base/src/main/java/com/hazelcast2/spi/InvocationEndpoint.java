package com.hazelcast2.spi;

public interface InvocationEndpoint {

    void send(byte[] bytes);
}
