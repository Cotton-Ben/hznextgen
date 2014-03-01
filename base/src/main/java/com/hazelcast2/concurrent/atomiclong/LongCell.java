package com.hazelcast2.concurrent.atomiclong;

/**
 * The LongCell is an exclusive cell. So at any given moment only a single thread can
 * be active.
 */
public class LongCell {

    public long value;
}
