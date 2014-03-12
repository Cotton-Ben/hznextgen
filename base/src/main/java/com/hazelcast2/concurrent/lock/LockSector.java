package com.hazelcast2.concurrent.lock;

import com.hazelcast2.spi.Sector;
import com.hazelcast2.spi.cellbased.CellBasedSector;
import com.hazelcast2.spi.cellbased.CellSectorOperation;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@CellBasedSector
public abstract class LockSector extends Sector {

    private final AtomicLong idGenerator = new AtomicLong();

    //todo: very inefficient structure.
    public final Map<Long, LockCell> cells = new HashMap<Long, LockCell>();

    public LockSector(LockSectorSettings settings) {
        super(settings);
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

    // ==================================================================================
    //                      isLocked
    // ==================================================================================

    public abstract boolean doIsLocked(long id, long threadId);

    @CellSectorOperation
    public boolean isLocked(LockCell cell, long threadId) {
        return cell.lockOwnerThreadId != -1;
    }


    // ==================================================================================
    //                      tryLock
    // ==================================================================================

    public abstract boolean doTryLock(long id, long threadId);

    @CellSectorOperation
    public boolean tryLock(LockCell cell, long threadId) {
        if (cell.lockOwnerThreadId != -1) {
            return false;
        }

        cell.lockOwnerThreadId = threadId;
        return true;
    }

    // ==================================================================================
    //                      unlock
    // ==================================================================================

    public abstract void doUnlock(long id, long threadId);

    @CellSectorOperation
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
