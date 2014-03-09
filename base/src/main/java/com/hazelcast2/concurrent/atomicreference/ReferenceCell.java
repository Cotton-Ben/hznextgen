package com.hazelcast2.concurrent.atomicreference;

/**
 * The LongCell is an exclusive cell. So at any given moment only a single thread can
 * be active.
 */
public class ReferenceCell {

    public Object value;
}
