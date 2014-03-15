package com.hazelcast2.spi;

/**
 * Generates id's.
 *
 * This implementation is not threadsafe. It can be used by sectors since they will be executed
 * by a single thread at any given moment.
 */
public class IdGenerator {

    private long value;

    public long nextId() {
        value++;
        return value;
    }
}
