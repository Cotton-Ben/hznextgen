package com.hazelcast2.concurrent.lock.impl;

import com.hazelcast2.concurrent.lock.LockConfig;
import com.hazelcast2.spi.IdGenerator;
import com.hazelcast2.spi.Sector;
import com.hazelcast2.spi.SectorClass;
import com.hazelcast2.spi.SectorOperation;

import java.util.HashMap;
import java.util.Map;

@SectorClass
public abstract class LockSector extends Sector {

    private final IdGenerator idGenerator = new IdGenerator();
    //todo: very inefficient structure.
    public final Map<Long, LockCell> cells = new HashMap<>();
    private final HashMap<String, Long> cellsId = new HashMap<>();

    public LockSector(LockSectorSettings settings) {
        super(settings);
    }

    @SectorOperation
    public long createCell(LockConfig config) {
        Long found = cellsId.get(config.name);
        if (found != null) {
            return found;
        }

        LockCell cell = new LockCell();
        cell.config = config;
        Long id = idGenerator.nextId();
        cells.put(id, cell);
        cellsId.put(config.name, id);
        return id;
    }

    public LockCell loadCell(long id) {
        return cells.get(id);
    }

    // ==================================================================================
    //                      destroy
    // ==================================================================================

    public abstract void doDestroy(long id);

    @SectorOperation
    public void destroy(long id) {
        LockCell cell = cells.remove(id);
        if (cell == null) {
            return;
        }

        cellsId.remove(cell.config.name);
    }

    public abstract long doIsDestroyed(long id);

    @SectorOperation(readonly = true)
    public long isDestroyed(long id) {
        return cells.containsKey(id) ? 1 : 0;
    }

    // ==================================================================================
    //                      isLocked
    // ==================================================================================

    public abstract boolean doIsLocked(long id, long threadId);

    @SectorOperation(cellbased = true, readonly = true)
    public boolean isLocked(LockCell cell, long threadId) {
        return cell.lockOwnerThreadId != -1;
    }


    // ==================================================================================
    //                      tryLock
    // ==================================================================================

    public abstract boolean doTryLock(long id, long threadId);

    @SectorOperation(cellbased = true)
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

    @SectorOperation(cellbased = true)
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
