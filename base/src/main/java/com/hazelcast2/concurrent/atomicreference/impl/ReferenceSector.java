package com.hazelcast2.concurrent.atomicreference.impl;

import com.hazelcast2.concurrent.atomicreference.AtomicReferenceConfig;
import com.hazelcast2.spi.Sector;
import com.hazelcast2.spi.cellbased.CellBasedSector;
import com.hazelcast2.spi.cellbased.SectorOperation;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

@CellBasedSector
public abstract class ReferenceSector extends Sector {

    private final AtomicLong idGenerator = new AtomicLong();

    //todo: very inefficient structure.
    private final Map<Long, ReferenceCell> cells = new HashMap<>();
    private final HashMap<String, Long> cellsId = new HashMap<>();

    public ReferenceSector(ReferenceSectorSettings settings) {
        super(settings);
    }

    @SectorOperation
    public long createCell(AtomicReferenceConfig config) {
        Long id = cellsId.get(config.name);
        if (id != null) {
            return id;
        }

        ReferenceCell cell = new ReferenceCell();
        cell.config = config;
        id = idGenerator.incrementAndGet();
        cells.put(id, cell);
        cellsId.put(config.name, id);
        return id;
    }

    public ReferenceCell loadCell(long id) {
        return cells.get(id);
    }

    // ==================================================================================
    //                      destroy
    // ==================================================================================

    public abstract void doDestroy(long id);

    @SectorOperation
    public void destroy(long id) {
        ReferenceCell cell = cells.remove(id);
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

    // ==================== get ================================================

    public abstract Object doGet(long id);

    public abstract Future<Object> asyncDoGet(long id);

    @SectorOperation(cellbased = true, readonly = true)
    public Object get(ReferenceCell cell) {
        return cell.value;
    }

    // ==================== isNull ================================================

    public abstract boolean doIsNull(long id);

    public abstract Future<Boolean> asyncDoIsNull(long id);

    @SectorOperation(cellbased = true, readonly = true)
    public boolean isNull(ReferenceCell cell) {
        return cell.value == null;
    }

    // ==================== set ================================================

    public abstract void doSet(long id, Object update);

    public abstract Future<Void> asyncDoSet(long id, Object update);

    @SectorOperation(cellbased = true)
    public void set(ReferenceCell cell, Object update) {
        cell.value = update;
    }

    // ==================== compare and set =====================================

    public abstract boolean doCompareAndSet(long id, Object expect, Object update);

    public abstract Future<Boolean> asyncDoCompareAndSet(long id, Object expect, Object update);

    @SectorOperation(cellbased = true)
    public boolean compareAndSet(ReferenceCell cell, Object expect, Object update) {
        if (cell.value != expect) {
            return false;
        }

        cell.value = update;
        return true;
    }
}