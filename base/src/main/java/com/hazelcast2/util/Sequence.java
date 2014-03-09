package com.hazelcast2.util;

import com.hazelcast2.utils.UnsafeHelper;
import sun.misc.Unsafe;

/**
 * Based on the Sequence of the LMAX Disruptor.
 * <p/>
 * todo:
 * The new sequencer uses an array
 * <p/>
 * FOr the padding, can't we rely on @Contended.
 */
public final class Sequence {

    private static final Unsafe unsafe = UnsafeHelper.findUnsafe();
    private static final long valueOffset;

    static {
        try {
            valueOffset = unsafe.objectFieldOffset
                    (Sequence.class.getDeclaredField("value"));
        } catch (Exception ex) {
            throw new Error(ex);
        }
    }

    private volatile long value;
    // cache line padding
    public long p1, p2, p3, p4, p5, p6, p7;

    public Sequence(long value) {
        this.value = value;
    }

    public void set(long value) {
        this.value = value;
    }

    public long get() {
        return value;
    }

    public boolean compareAndSet(long expect, long update) {
        return unsafe.compareAndSwapLong(this, valueOffset, expect, update);
    }

    public void inc(int amount) {
        if (amount == 0) {
            return;
        }

        for (; ; ) {
            long oldValue = value;
            if (compareAndSet(oldValue, oldValue + amount)) {
                return;
            }
        }
    }

    public void inc() {
        inc(1);
    }
}
