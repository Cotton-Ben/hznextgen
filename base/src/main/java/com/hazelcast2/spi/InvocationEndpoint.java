package com.hazelcast2.spi;

public interface InvocationEndpoint {
    void invoke(byte[] bytes);
}
