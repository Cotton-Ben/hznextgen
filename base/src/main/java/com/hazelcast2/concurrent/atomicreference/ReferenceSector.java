package com.hazelcast2.concurrent.atomicreference;

import com.hazelcast2.spi.PartitionSettings;
import com.hazelcast2.spi.Sector;
import com.hazelcast2.spi.cellbased.CellBasedSector;
import com.hazelcast2.spi.cellbased.CellSectorOperation;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

@CellBasedSector
public abstract class ReferenceSector extends Sector {

    private final AtomicLong idGenerator = new AtomicLong();

    //todo: very inefficient structure.
    private final Map<Long, ReferenceCell> cells = new HashMap<Long, ReferenceCell>();

    public ReferenceSector(PartitionSettings partitionSettings) {
        super(partitionSettings);
    }

    public long createCell() {
        ReferenceCell cell = new ReferenceCell();
        long id = idGenerator.incrementAndGet();
        cells.put(id, cell);
        return id;
    }

    public ReferenceCell loadCell(long id) {
        return cells.get(id);
    }

    // ==================== get ================================================

    public abstract Object doGet(long id);

    public abstract Future<Object> asyncDoGet(long id);

    @CellSectorOperation(readonly = true)
    public Object get(ReferenceCell cell) {
        return cell.value;
    }

    // ==================== set ================================================

    public abstract void doSet(long id, Object update);

    public abstract Future<Void> asyncDoSet(long id, Object update);

    @CellSectorOperation
    public void set(ReferenceCell cell, Object update) {
        cell.value = update;
    }

    // ==================== compare and set =====================================

    public abstract boolean doCompareAndSet(long id, Object expect, Object update);

    public abstract Future<Boolean> asyncDoCompareAndSet(long id, Object expect, Object update);

    @CellSectorOperation
    public boolean compareAndSet(ReferenceCell cell, Object expect, Object update) {
        if (cell.value != expect) {
            return false;
        }

        cell.value = update;
        return true;
    }
}