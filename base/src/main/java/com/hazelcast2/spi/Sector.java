package com.hazelcast2.spi;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;

/**
 * In theory the Sector is not needed. Since we are going to generate Sector subclasses for the missing
 * methods, we can easily add these methods as well.
 */
public abstract class Sector {

    private static final AtomicLongFieldUpdater<Sector> PRODUCER_UPDATER
            = AtomicLongFieldUpdater.newUpdater(Sector.class, "prodSeq");

    public static final long CLAIM_SLOT_LOCKED = -2;
    public static final long CLAIM_SLOT_NO_CAPACITY = -3;
    public static final int DELTA = 4;
    public static final long INITIAL_VALUE = 0;
    public static final int MASK_LOCKED = 1;
    public static final int MASK_SCHEDULED = 2;

    private volatile boolean isLocked;
    private final int partitionId;
    public final SectorScheduler scheduler;

    //this sucks because both of them will probably fall in the same cache line.
    public volatile long prodSeq = INITIAL_VALUE;
    public volatile long conSeq = INITIAL_VALUE;

    public final Invocation[] ringbuffer;
    public final int ringbufferSize;

    public Sector(PartitionSettings partitionSettings) {
        if (partitionSettings == null) {
            throw new NullPointerException();
        }
        if (partitionSettings.getPartitionId() < 0) {
            throw new IllegalArgumentException();
        }
        this.partitionId = partitionSettings.getPartitionId();
        this.scheduler = partitionSettings.scheduler;
        this.ringbufferSize = partitionSettings.ringbufferSize;
        this.ringbuffer = new Invocation[ringbufferSize];
        for (int k = 0; k < ringbuffer.length; k++) {
            ringbuffer[k] = new Invocation();
        }
    }

    /**
     * Processes all pending requests for this partition.
     */
    public abstract void process();

    /**
     * Returns the id of the partition this segment belongs to.
     *
     * @return the partition id.
     */
    public final int getPartitionId() {
        return partitionId;
    }

    /**
     * Checks if this Sector currently is locked for system operations.
     *
     * @return true if is locked, false otherwise.
     */
    public final boolean isSystemLocked() {
        return isLocked;
    }

    /**
     * Locks this Sector for system operations.
     * <p/>
     * This call is not thread-safe; only a single thread should call this method.
     * <p/>
     * A future is returned because it can take time to lock the segment; imagine that a thread is doing some
     * calculation on an IAtomicLong which takes a lot of time, than the locking will not be complete till this
     * operation completes.
     *
     * @return the future pointing to this lock.
     */
    public final Future systemLock() {
        throw new UnsupportedOperationException();
    }

    /**
     * Unlocks this Sector so that user operations can run on it.  The systemUnlock method should only be called
     * after the lock is complete (so after the future.get has returned successfully).
     * <p/>
     * This call is not thread-safe; only a single thread should call this method.
     */
    public final void systemUnlock() {
        throw new UnsupportedOperationException();
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
        //todo: a loop should not be needed since only one thread will call systemUnlock and no threads can update
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
