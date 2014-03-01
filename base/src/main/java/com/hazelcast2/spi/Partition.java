package com.hazelcast2.spi;

import com.hazelcast2.util.CountdownFuture;
import com.hazelcast2.util.ExceptionFuture;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

/**
 * In theory the Partition is not needed. Since we are going to generate Partition subclasses for the missing
 * methods, we can easily add these methods as well.
 */
public abstract class Partition {

     private volatile boolean isLocked;

    //todo: padding to prevent false sharing
    //todo: configurable number of items in the segments.
    private final Segment[] segments;
    private final int partitionId;
    public final Scheduler scheduler;

    public Partition(PartitionSettings partitionSettings) {
        if(partitionSettings == null){
            throw new NullPointerException();
        }
        if (partitionSettings.getPartitionId() < 0) {
            throw new IllegalArgumentException();
        }
        this.partitionId = partitionSettings.getPartitionId();
        this.scheduler = partitionSettings.scheduler;

        segments = new Segment[partitionSettings.getSegmentCount()];
        for (int k = 0; k < segments.length; k++) {
            segments[k] = createSegment();
        }
    }

    public final int getSegmentIndex(long id) {
        int hash = (int) (id ^ (id >>> 32));
        if (hash == Integer.MIN_VALUE) {
            hash = Integer.MAX_VALUE;
        } else if (hash < 0) {
            hash = -hash;
        }

        return hash % segments.length;
    }

    public Segment createSegment() {
        return new Segment(64);
    }

    public final Segment getSegment(int segmentIndex) {
        return segments[segmentIndex];
    }

    /**
     * Processes all pending requests for the given segment.
     *
     * @param segment
     */
    public abstract void process(Segment segment);

    /**
     * Returns the id of the partition this segment belongs to.
     *
     * @return the partition id.
     */
    public final int getPartitionId() {
        return partitionId;
    }

    public final int getSegmentLength() {
        return segments.length;
    }

    /**
     * Checks if this Partition currently is locked for system operations.
     *
     * @return true if is locked, false otherwise.
     */
    public final boolean isSystemLocked() {
        return isLocked;
    }

    /**
     * Locks this Partition for system operations.
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
        if (isLocked) {
            return new ExceptionFuture(
                    new IllegalStateException("Partition " + partitionId + " already locked"));
        }

        isLocked = true;
        int segmentLength = segments.length;
        final CountDownLatch countDownLatch = new CountDownLatch(segmentLength);
        for (int segmentIndex = 0; segmentIndex < segmentLength; segmentIndex++) {
            Segment segment = segments[segmentIndex];
            segment.lock();
            //todo: waiting needs to be added
        }

        return new CountdownFuture(countDownLatch);
    }

    /**
     * Unlocks this Partition so that user operations can run on it.  The systemUnlock method should only be called
     * after the lock is complete (so after the future.get has returned successfully).
     * <p/>
     * This call is not thread-safe; only a single thread should call this method.
     */
    public final void systemUnlock() {
        for (int stripeIndex = 0; stripeIndex < segments.length; stripeIndex++) {
            final Segment segment = segments[stripeIndex];
            segment.unlock();
        }
        isLocked = false;
    }
}
