package com.hazelcast2.spi;

import java.util.concurrent.atomic.AtomicLongFieldUpdater;

public class Segment {

    public static final long CLAIM_SLOT_LOCKED = -2;
    public static final long CLAIM_SLOT_NO_CAPACITY = -3;
    public static final int DELTA = 4;
    public static final long INITIAL_VALUE = 0;
    public static final int MASK_LOCKED = 1;
    public static final int MASK_SCHEDULED = 2;

    private static final AtomicLongFieldUpdater<Segment> PRODUCER_UPDATER
            = AtomicLongFieldUpdater.newUpdater(Segment.class, "prodSeq");

    //this sucks because both of them will probably fall in the same cache line.
    public volatile long prodSeq = INITIAL_VALUE;
    public volatile long conSeq = INITIAL_VALUE;

    public final Invocation[] ringbuffer;
    public final int ringbufferSize;

    public Segment(int length) {
        this.ringbufferSize = length;
        this.ringbuffer = new Invocation[length];
        for (int k = 0; k < ringbuffer.length; k++) {
            ringbuffer[k] = new Invocation();
        }
    }

    public int size() {
        return (int) ((prodSeq >> 2) - conSeq);
    }

    public final long claimSlot() {
        for (; ; ) {
            final long oldProdSeq = prodSeq;

            if ((oldProdSeq & MASK_LOCKED) != 0) {
                return CLAIM_SLOT_LOCKED;
            }

            long p = oldProdSeq >> 2;
            if (conSeq + ringbufferSize == p) {
                return CLAIM_SLOT_NO_CAPACITY;
            }

            if ((oldProdSeq & MASK_SCHEDULED) != 0) {
                //it is already scheduled.
                if (PRODUCER_UPDATER.compareAndSet(this, oldProdSeq, oldProdSeq + DELTA)) {
                    //we need to unset the scheduled bit to indicate the caller that he was not
                    //the one that managed the schedule this segment.
                    return oldProdSeq - MASK_SCHEDULED;
                }
            } else {
                //it isn't scheduled. So we are going to try to schedule it.
                if (PRODUCER_UPDATER.compareAndSet(this, oldProdSeq, oldProdSeq + DELTA + MASK_SCHEDULED)) {
                    return oldProdSeq + MASK_SCHEDULED;
                }
            }
        }
    }

    public boolean unschedule() {
        final long currentConSeq = conSeq;
        for (; ; ) {
            final long oldProdSeq = prodSeq;

            if ((oldProdSeq & MASK_SCHEDULED) == 0) {
                throw new IllegalStateException();
            }

            if ((prodSeq >> 2) > currentConSeq) {
                return false;
            }

            if (PRODUCER_UPDATER.compareAndSet(this, oldProdSeq, oldProdSeq - MASK_SCHEDULED)) {
                return true;
            }
        }
    }

    private int toIndex(final long seq) {
        //todo: make use of bit-shifting to determine the mod.
        return (int) (seq % ringbufferSize);
    }

    public final Invocation getSlot(final long seq) {
        return ringbuffer[toIndex(seq)];
    }

    /**
     * Locks the segment. Once it has locked, no slots can be claimed (so no invocations
     * can be done).
     * <p/>
     * Method should not be called concurrently; so should only be called by a system thread.
     *
     * @throws java.lang.IllegalStateException if the lock is already acquired.
     */
    public final void lock() {
        for (; ; ) {
            final long oldProdSeq = prodSeq;

            if ((oldProdSeq & MASK_LOCKED) != 0) {
                throw new IllegalStateException("Already locked");
            }

            if (PRODUCER_UPDATER.compareAndSet(this, oldProdSeq, oldProdSeq + MASK_LOCKED)) {
                return;
            }
        }
    }

    /**
     * Unlocks the segment. Once it is unlocked, slots can be claimed.
     * <p/>
     * Method should not be called concurrently; so should only be called by a system thread.
     *
     * @throws java.lang.IllegalStateException if the Segment isn't locked.
     */
    public void unlock() {
        //todo: a loop should not be needed since only one thread will call unlock and no threads can update
        //the prod sequence while the lock is hold.

        for (; ; ) {
            final long oldProdSeq = prodSeq;

            if ((oldProdSeq & MASK_LOCKED) == 0) {
                throw new IllegalStateException();
            }

            if (PRODUCER_UPDATER.compareAndSet(this, oldProdSeq, oldProdSeq - MASK_LOCKED)) {
                return;
            }
        }
    }

    public final boolean isLocked() {
        return isLocked(prodSeq);
    }

    public final boolean isScheduled() {
        return isScheduled(prodSeq);
    }

    public static boolean isLocked(final long seq) {
        return (seq & MASK_LOCKED) != 0;
    }

    public static boolean isScheduled(final long seq) {
        return (seq & MASK_SCHEDULED) != 0;
    }

}
