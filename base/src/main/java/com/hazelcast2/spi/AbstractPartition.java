package com.hazelcast2.spi;

import com.hazelcast2.util.CountdownFuture;
import com.hazelcast2.util.ExceptionFuture;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

/**
 * In theory the AbstractPartition is not needed. Since we are going to generate Partition subclasses for the missing
 * methods, we can easily add these methods as well.
 */
public abstract class AbstractPartition implements Partition {

     private volatile boolean isLocked;

    //todo: padding to prevent false sharing
    //todo: configurable number of items in the segments.
    private final Segment[] segments;
    private final int partitionId;
    public final Scheduler scheduler;

    public AbstractPartition(PartitionSettings partitionSettings) {
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

    @Override
    public final int getPartitionId() {
        return partitionId;
    }

    public final int getSegmentLength() {
        return segments.length;
    }

    @Override
    public final boolean isLocked() {
        return isLocked;
    }

    @Override
    public final Future lock() {
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

    //private void cancelAll(Invocation current) {
    //    while (current != null) {
    //        current.cancel();
    //        //current = current.previous;
    //    }
    //}

    @Override
    public final void unlock() {
        for (int stripeIndex = 0; stripeIndex < segments.length; stripeIndex++) {
            final Segment segment = segments[stripeIndex];
            segment.unlock();
        }
        isLocked = false;
    }
}
