package com.hazelcast2.lock;

import com.hazelcast2.spi.AbstractPartition;
import com.hazelcast2.spi.OperationMethod;
import com.hazelcast2.spi.PartitionAnnotation;
import com.hazelcast2.spi.PartitionSettings;
import com.hazelcast2.spi.Segment;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@PartitionAnnotation
public abstract class LockPartition extends AbstractPartition {

    private final AtomicLong idGenerator = new AtomicLong();

    public LockPartition(PartitionSettings partitionSettings) {
        super(partitionSettings);
    }

    @Override
    public Segment createSegment() {
        return new LockSegment(64);
    }

    public long createCell() {
        LockCell cell = new LockCell();
        long id = idGenerator.incrementAndGet();
        int segmentIndex = getSegmentIndex(id);
        LockSegment segment = (LockSegment) getSegment(segmentIndex);
        segment.cells.put(id, cell);
        return id;
    }

    public LockCell loadCell(long id) {
        int segmentIndex = getSegmentIndex(id);
        LockSegment segment = (LockSegment) getSegment(segmentIndex);
        return segment.cells.get(id);
    }

    public abstract boolean doIsLocked(long id, long threadId);

    @OperationMethod
    public boolean isLocked(LockCell cell, long threadId) {
        return cell.lockOwnerThreadId != -1;
    }

    public abstract boolean doTryLock(long id, long threadId);

    @OperationMethod
    public boolean tryLock(LockCell cell, long threadId) {
        if (cell.lockOwnerThreadId != -1) {
            return false;
        }

        cell.lockOwnerThreadId = threadId;
        return true;
    }

    public abstract void doUnlock(long id, long threadId);


    @OperationMethod
    public void unlock(LockCell cell, long threadId) {
        if (cell.lockOwnerThreadId == -1) {
            throw new IllegalMonitorStateException();
        }

        if (cell.lockOwnerThreadId != threadId) {
            throw new IllegalMonitorStateException();
        }

        cell.lockOwnerThreadId = -1;
    }

    private static class LockSegment extends Segment {

        //very inefficient structure.
        public final Map<Long, LockCell> cells = new HashMap<Long, LockCell>();


        private LockSegment(int length) {
            super(length);
        }
    }
}
