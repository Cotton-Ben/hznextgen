package com.hazelcast2.spi;

import com.hazelcast2.serialization.SerializationService;
import com.hazelcast2.util.Sequence;

import java.util.concurrent.Future;

/**
 * A partition is composed of sectors. And there are specialized sectors like a sector for AtomicLongs or a sector
 * for MapEntries.
 * <p/>
 * A sector has a build in pending invocations queue to deal with concurrent operations. Within a sector only
 * 1 thread will be active. But within different sectors of the same partition, different threads can be active.
 * This is a fundamental difference compared to Hazelcast 3.
 */
public abstract class Sector {

    public static final long CLAIM_SLOT_LOCKED = -2;
    public static final long CLAIM_SLOT_NO_CAPACITY = -3;
    public static final int DELTA = 4;
    public static final long INITIAL_VALUE = 0;
    public static final int MASK_LOCKED = 1;
    public static final int MASK_SCHEDULED = 2;

    private volatile boolean isLocked;
    private final int partitionId;
    public final SectorScheduler scheduler;
    public final SerializationService serializationService;

    /**
     * The endpoints for a sector. So a sector can be in multiple states:
     * - it is the master
     * - it is a backup
     * - none of the above
     * In this endpoints you can find on the first position the master and after that you
     * get the replicas. These endpoints will be updated when partitions are moving around.
     * <p/>
     * The advantage of this approach instead of first looking up the replica index, and then
     * retrieving the connection, you immediately have the endpoints available without needing
     * to do any lookup.
     */
    public volatile InvocationEndpoint[] endpoints;
    public final Sequence prodSeq = new Sequence(INITIAL_VALUE);
    public final Sequence conSeq = new Sequence(INITIAL_VALUE);

    public final Invocation[] ringbuffer;
    public final int ringbufferSize;
    public final short serviceId;

    public Sector(SectorSettings settings) {
        if (settings == null) {
            throw new NullPointerException();
        }
        if (settings.partitionId < 0) {
            throw new IllegalArgumentException();
        }
        this.serviceId = settings.serviceId;
        this.partitionId = settings.partitionId;
        this.scheduler = settings.scheduler;
        this.serializationService = settings.serializationService;
        this.ringbufferSize = settings.ringbufferSize;
        this.ringbuffer = new Invocation[ringbufferSize];
        for (int k = 0; k < ringbuffer.length; k++) {
            ringbuffer[k] = new Invocation();
        }
    }

    /**
     * Processes all pending requests for this sector.
     */
    public abstract void process();

    /**
     * Returns the id of the partition this sector belongs to.
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
        return (int) ((prodSeq.get() >> 2) - conSeq.get());
    }

    public final long claimSlotAndReturnStatus() {
        for (; ; ) {
            final long oldProdSeq = prodSeq.get();

            if ((oldProdSeq & MASK_LOCKED) != 0) {
                return CLAIM_SLOT_LOCKED;
            }

            //todo: shitty name p.
            long p = oldProdSeq >> 2;
            if (conSeq.get() + ringbufferSize == p) {
                return CLAIM_SLOT_NO_CAPACITY;
            }

            if ((oldProdSeq & MASK_SCHEDULED) != 0) {
                //it is already scheduled.
                if (prodSeq.compareAndSet(oldProdSeq, oldProdSeq + DELTA)) {
                    //we need to unset the scheduled bit to indicate the caller that he was not
                    //the one that managed the schedule this segment.
                    return oldProdSeq - MASK_SCHEDULED;
                }
            } else {
                //it isn't scheduled. So we are going to try to schedule it.
                if (prodSeq.compareAndSet(oldProdSeq, oldProdSeq + DELTA + MASK_SCHEDULED)) {
                    return oldProdSeq + MASK_SCHEDULED;
                }
            }
        }
    }

    public boolean unschedule() {
        final long currentConSeq = conSeq.get();
        for (; ; ) {
            final long oldProdSeq = prodSeq.get();

            if ((oldProdSeq & MASK_SCHEDULED) == 0) {
                throw new IllegalStateException();
            }

            if ((prodSeq.get() >> 2) > currentConSeq) {
                return false;
            }

            if (prodSeq.compareAndSet(oldProdSeq, oldProdSeq - MASK_SCHEDULED)) {
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
            final long oldProdSeq = prodSeq.get();

            if ((oldProdSeq & MASK_LOCKED) != 0) {
                throw new IllegalStateException("Already locked");
            }

            if (prodSeq.compareAndSet(oldProdSeq, oldProdSeq + MASK_LOCKED)) {
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
            final long oldProdSeq = prodSeq.get();

            if ((oldProdSeq & MASK_LOCKED) == 0) {
                throw new IllegalStateException();
            }

            if (prodSeq.compareAndSet(oldProdSeq, oldProdSeq - MASK_LOCKED)) {
                return;
            }
        }
    }

    public final boolean isLocked() {
        return isLocked(prodSeq.get());
    }

    public final boolean isScheduled() {
        return isScheduled(prodSeq.get());
    }

    public static boolean isLocked(final long sequenceAndStatus) {
        return (sequenceAndStatus & MASK_LOCKED) != 0;
    }

    public static boolean isScheduled(final long sequenceAndStatus) {
        return (sequenceAndStatus & MASK_SCHEDULED) != 0;
    }

    public static long getSequence(final long sequenceAndStatus) {
        return sequenceAndStatus >> 2;
    }

    /**
     * The schedule wants to schedule a operation invocation on this sector.
     *
     * @param bytes
     */
    public abstract void schedule(byte[] bytes);
}
