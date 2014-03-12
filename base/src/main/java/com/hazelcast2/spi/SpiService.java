package com.hazelcast2.spi;

public interface SpiService {

    void schedule(byte[] invocationBytes);
}
