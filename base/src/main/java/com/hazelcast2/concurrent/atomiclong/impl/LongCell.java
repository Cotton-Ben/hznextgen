package com.hazelcast2.concurrent.atomiclong.impl;

import com.hazelcast2.concurrent.atomiclong.AtomicLongConfig;

/**
 * The LongCell is an exclusive cell. So at any given moment only a single thread can
 * be active.
 */
public class LongCell {
    public AtomicLongConfig config;
    public long value;
}
