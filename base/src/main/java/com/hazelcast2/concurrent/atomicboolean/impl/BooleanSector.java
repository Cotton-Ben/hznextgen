package com.hazelcast2.concurrent.atomicboolean.impl;

import com.hazelcast2.concurrent.atomicboolean.AtomicBooleanConfig;
import com.hazelcast2.spi.Sector;
import com.hazelcast2.spi.SectorClass;
import com.hazelcast2.spi.SectorOperation;

import java.util.HashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

@SectorClass
public abstract class BooleanSector extends Sector {

    //todo: very inefficient structure.
    private final HashMap<Long, BooleanCell> cells = new HashMap<>();
    private final HashMap<String, Long> cellsId = new HashMap<>();

    private final AtomicLong idGenerator = new AtomicLong();

    public BooleanSector(BooleanSectorSettings sectorSettings) {
        super(sectorSettings);
    }

    @SectorOperation
    public long createCell(AtomicBooleanConfig config) {
        Long id = cellsId.get(config.name);
        if (id != null) {
            return id;
        }

        BooleanCell cell = new BooleanCell();
        cell.config = config;
        id = idGenerator.incrementAndGet();
        cells.put(id, cell);
        cellsId.put(config.name, id);
        return id;
    }

    public BooleanCell loadCell(long id) {
        return cells.get(id);
    }

    // ==================================================================================
    //                      destroy
    // ==================================================================================

    public abstract void doDestroy(long id);

    @SectorOperation
    public void destroy(long id) {
        BooleanCell cell = cells.remove(id);
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
    //                      get
    // ==================================================================================

    public abstract boolean doGet(long id);

    public abstract Future<Boolean> asyncDoGet(long id);

    @SectorOperation(readonly = true,cellbased = true)
    public boolean get(BooleanCell cell) {
        return cell.value;
    }

    // ==================================================================================
    //                      set
    // ==================================================================================

    public abstract boolean doSet(long id, boolean update);

    public abstract Future<Boolean> asyncDoSet(long id, boolean update);

    @SectorOperation(cellbased = true)
    public boolean set(BooleanCell cell, boolean update) {
        boolean oldValue = cell.value;
        cell.value = update;
        return oldValue;
    }

    // ==================================================================================
    //                      compareAndSet
    // ==================================================================================

    public abstract boolean doCompareAndSet(long id, boolean old, boolean update);

    public abstract Future<Boolean> asyncDoCompareAndSet(long id, boolean old, boolean update);

    @SectorOperation(cellbased = true)
    public boolean compareAndSet(BooleanCell cell, boolean old, boolean update) {
        if (cell.value != old) {
            return false;
        }

        cell.value = update;
        return true;
    }
}