package com.hazelcast2.lock;

import com.hazelcast2.spi.Partition;
import com.hazelcast2.spi.OperationMethod;
import com.hazelcast2.spi.PartitionAnnotation;
import com.hazelcast2.spi.PartitionSettings;

import javax.swing.text.Segment;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@PartitionAnnotation
public abstract class LockPartition extends Partition {

    private final AtomicLong idGenerator = new AtomicLong();

    //very inefficient structure.
    public final Map<Long, LockCell> cells = new HashMap<Long, LockCell>();

    public LockPartition(PartitionSettings partitionSettings) {
        super(partitionSettings);
    }

    public long createCell() {
        LockCell cell = new LockCell();
        long id = idGenerator.incrementAndGet();
        cells.put(id, cell);
        return id;
    }

    public LockCell loadCell(long id) {
        return cells.get(id);
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
}
