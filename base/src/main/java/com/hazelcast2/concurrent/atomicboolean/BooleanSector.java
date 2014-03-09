package com.hazelcast2.concurrent.atomicboolean;

import com.hazelcast2.spi.PartitionSettings;
import com.hazelcast2.spi.Sector;
import com.hazelcast2.spi.cellbased.CellBasedSector;
import com.hazelcast2.spi.cellbased.CellSectorOperation;

import java.util.HashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

@CellBasedSector
public abstract class BooleanSector extends Sector {

    //todo: very inefficient structure.
    private final HashMap<Long, BooleanCell> cells = new HashMap<>();

    private final AtomicLong idGenerator = new AtomicLong();

    public BooleanSector(PartitionSettings partitionSettings) {
        super(partitionSettings);
    }

    public long createCell() {
        BooleanCell cell = new BooleanCell();
        long id = idGenerator.incrementAndGet();
        cells.put(id, cell);
        return id;
    }

    public BooleanCell loadCell(long id) {
        return cells.get(id);
    }

    // ================== set ============================================================

    public abstract boolean doSet(long id, boolean update);

    public abstract Future<Boolean> asyncDoSet(long id, boolean update);

    @CellSectorOperation
    public boolean set(BooleanCell cell, boolean update) {
        boolean oldValue = cell.value;
        cell.value = update;
        return oldValue;
    }

    // ================== get ============================================================

    public abstract boolean doGet(long id);

    public abstract Future<Boolean> asyncDoGet(long id);

    @CellSectorOperation
    public boolean get(BooleanCell cell) {
        return cell.value;
    }

    // ================== compareAndSet =================================================

    public abstract boolean doCompareAndSet(long id, boolean old, boolean update);

    public abstract Future<Boolean> asyncDoCompareAndSet(long id, boolean old, boolean update);

    @CellSectorOperation
    public boolean compareAndSet(BooleanCell cell, boolean old, boolean update) {
        if (cell.value != old) {
            return false;
        }

        cell.value = update;
        return true;
    }

}