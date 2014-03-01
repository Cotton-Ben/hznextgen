package com.hazelcast2.spi;

import java.util.concurrent.Future;

/**
 * The memory for a partition is split up in multiple segments, e.g. a segment for an {@link com.hazelcast2.IAtomicLong}
 * or segment for an {@link com.hazelcast2.IAtomicLong}.
 * <p/>
 * Each 'service' will probably have its own segments like with IMap and IAtomicLong.
 */
public interface Partition {

    /**
     * Processes all pending requests for the given segment.
     *
     * @param segment
     */
    void process(Segment segment);

    /**
     * Checks if this Partition currently is locked for system operations.
     *
     * @return true if is locked, false otherwise.
     */
    boolean isLocked();

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
    Future lock();

    /**
     * Unlocks this Partition so that user operations can run on it.  The unlock method should only be called
     * after the lock is complete (so after the future.get has returned successfully).
     * <p/>
     * This call is not thread-safe; only a single thread should call this method.
     */
    void unlock();

    /**
     * Returns the id of the partition this segment belongs to.
     *
     * @return the partition id.
     */
    int getPartitionId();
}
