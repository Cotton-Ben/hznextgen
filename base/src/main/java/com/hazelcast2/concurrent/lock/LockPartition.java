package com.hazelcast2.concurrent.lock;

import com.hazelcast2.spi.cellbased.CellPartitionOperation;
import com.hazelcast2.spi.Partition;
import com.hazelcast2.spi.cellbased.CellBasedPartition;
import com.hazelcast2.spi.PartitionSettings;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@CellBasedPartition
public abstract class LockPartition extends Partition {

    private final AtomicLong idGenerator = new AtomicLong();

    //todo: very inefficient structure.
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

    @CellPartitionOperation
    public boolean isLocked(LockCell cell, long threadId) {
        return cell.lockOwnerThreadId != -1;
    }

    public abstract boolean doTryLock(long id, long threadId);

    @CellPartitionOperation
    public boolean tryLock(LockCell cell, long threadId) {
        if (cell.lockOwnerThreadId != -1) {
            return false;
        }

        cell.lockOwnerThreadId = threadId;
        return true;
    }

    public abstract void doUnlock(long id, long threadId);

    @CellPartitionOperation
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
