package com.hazelcast2.concurrent.atomiclong.impl;

import com.hazelcast2.concurrent.atomiclong.AtomicLongConfig;
import com.hazelcast2.core.LongFunction;
import com.hazelcast2.spi.Sector;
import com.hazelcast2.spi.cellbased.CellBasedSector;
import com.hazelcast2.spi.cellbased.SectorOperation;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

@CellBasedSector
public abstract class LongSector extends Sector {

    private final AtomicLong idGenerator = new AtomicLong();

    //todo: very inefficient structure.
    private final Map<Long, LongCell> cells = new HashMap<>();
    private final HashMap<String, Long> cellsId = new HashMap<>();

    public LongSector(LongSectorSettings settings) {
        super(settings);
    }

    @SectorOperation
    public long createCell(AtomicLongConfig config) {
        Long id = cellsId.get(config.name);
        if (id != null) {
            return id;
        }

        LongCell cell = new LongCell();
        cell.config = config;
        id = idGenerator.incrementAndGet();
        cells.put(id, cell);
        cellsId.put(config.name, id);
        return id;
    }


    public LongCell loadCell(long id) {
        return cells.get(id);
    }


    // ==================================================================================
    //                      destroy
    // ==================================================================================


    @SectorOperation
    public void destroy(long id) {
        LongCell cell = cells.remove(id);
        if (cell == null) {
            return;
        }

        cellsId.remove(cell.config.name);
    }

    @SectorOperation(readonly = true)
    public long isDestroyed(long id) {
        return cells.containsKey(id) ? 1 : 0;
    }


    // ==================================================================================
    //                      get
    // ==================================================================================


    public abstract long doGet(long id);

    public abstract Future<Long> asyncDoGet(long id);

    @SectorOperation(readonly = true,cellbased = true)
    public long get(LongCell cell) {
        return cell.value;
    }

    // ==================================================================================
    //                      set
    // ==================================================================================


    public abstract void doSet(long id, long update);

    public abstract Future<Void> asyncDoSet(long id, long update);

    @SectorOperation(cellbased = true)
    public void set(LongCell cell, long update) {
        cell.value = update;
    }


    // ==================================================================================
    //                      inc
    // ==================================================================================


    public abstract void doInc(long id);

    public abstract Future<Void> asyncDoInc(long id);

    @SectorOperation(cellbased = true)
    public void inc(LongCell cell) {
        cell.value++;
    }

    // ==================================================================================
    //                      compareAndSet
    // ==================================================================================


    public abstract boolean doCompareAndSet(long id, long expect, long update);

    public abstract Future<Boolean> asyncDoCompareAndSet(long id, long expect, long update);

    @SectorOperation(cellbased = true)
    public boolean compareAndSet(LongCell cell, long expect, long update) {
        if (cell.value != expect) {
            return false;
        }

        cell.value = update;
        return true;
    }

    // ==================================================================================
    //                      apply
    // ==================================================================================

    public abstract long doApply(long id, LongFunction f);

    public abstract Future<Long> asyncDoApply(long id, LongFunction f);

    @SectorOperation(cellbased = true, readonly = true)
    public long apply(LongCell cell, LongFunction f) {
        return f.apply(cell.value);
    }

    // ==================================================================================
    //                      alter
    // ==================================================================================

    public abstract void doAlter(long id, LongFunction f);

    public abstract Future<Void> asyncDoAlter(long id, LongFunction f);

    @SectorOperation(cellbased = true)
    public void alter(LongCell cell, LongFunction f) {
        cell.value = f.apply(cell.value);
    }

}