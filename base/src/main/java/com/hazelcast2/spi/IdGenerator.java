package com.hazelcast2.spi;

/**
 * Generates id's.
 *
 * This implementation is not threadsafe. It can be used by sectors since they will be executed
 * by a single thread at any given moment.
 *
 * When we are going to do id compression, e.g. instead of needing to use 64 bit, 32 bit is used
 * for low addresses, it is good to have that logic here.
 */
public class IdGenerator {

    private long value;

    public long nextId() {
        value++;
        return value;
    }
}
