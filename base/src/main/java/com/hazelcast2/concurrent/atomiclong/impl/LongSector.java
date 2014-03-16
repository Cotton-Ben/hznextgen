package com.hazelcast2.concurrent.atomiclong.impl;

import com.hazelcast2.concurrent.atomiclong.AtomicLongConfig;
import com.hazelcast2.core.IdNotFoundException;
import com.hazelcast2.core.LongFunction;
import com.hazelcast2.spi.IdGenerator;
import com.hazelcast2.spi.Sector;
import com.hazelcast2.spi.SectorClass;
import com.hazelcast2.spi.SectorOperation;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

@SectorClass
public abstract class LongSector extends Sector {

    private final IdGenerator idGenerator = new IdGenerator();

    //todo: very inefficient structure.
    private final Map<Long, LongCell> cells = new HashMap<>();
    private final HashMap<String, Long> cellsId = new HashMap<>();

    public LongSector(LongSectorSettings settings) {
        super(settings);
    }

    public LongCell loadCell(long id) {
        LongCell cell = cells.get(id);
        if(cell == null){
            throw new IdNotFoundException("Can't find LongCell for id:"+id+" partition:"+partitionId+". " +
                    "It is very likely that the IAtomicLong has been destroyed.");
        }

        return cell;
    }

    public abstract long hz_createCell(AtomicLongConfig config);

    @SectorOperation
    public long createCell(AtomicLongConfig config) {
        Long found = cellsId.get(config.name);
        if (found != null) {
            return found;
        }

        LongCell cell = new LongCell();
        cell.config = config;
        long id = idGenerator.nextId();
        cells.put(id, cell);
        cellsId.put(config.name, id);
        return id;
    }

    // ==================================================================================
    //                      destroy
    // ==================================================================================

    public abstract void hz_destroy(long id);

    @SectorOperation
    public void destroy(long id) {
        LongCell cell = cells.remove(id);
        if (cell == null) {
            return;
        }

        cellsId.remove(cell.config.name);
    }

    public abstract long hz_isDestroyed(long id);

    @SectorOperation(readonly = true)
    public long isDestroyed(long id) {
        return cells.containsKey(id) ? 1 : 0;
    }


    // ==================================================================================
    //                      get
    // ==================================================================================


    public abstract long hz_get(long id);

    public abstract Future<Long> hz_asyncGet(long id);

    @SectorOperation(readonly = true,cellbased = true)
    public long get(LongCell cell) {
        return cell.value;
    }

    // ==================================================================================
    //                      set
    // ==================================================================================


    public abstract void hz_set(long id, long update);

    public abstract Future<Void> hz_asyncSet(long id, long update);

    @SectorOperation(cellbased = true)
    public void set(LongCell cell, long update) {
        cell.value = update;
    }


    // ==================================================================================
    //                      inc
    // ==================================================================================


    public abstract void hz_inc(long id);

    public abstract Future<Void> hz_asyncInc(long id);

    @SectorOperation(cellbased = true)
    public void inc(LongCell cell) {
        cell.value++;
    }

    // ==================================================================================
    //                      compareAndSet
    // ==================================================================================


    public abstract boolean hz_compareAndSet(long id, long expect, long update);

    public abstract Future<Boolean> hz_asyncCompareAndSet(long id, long expect, long update);

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

    public abstract long hz_apply(long id, LongFunction f);

    public abstract Future<Long> hz_asyncApply(long id, LongFunction f);

    @SectorOperation(cellbased = true, readonly = true)
    public long apply(LongCell cell, LongFunction f) {
        return f.apply(cell.value);
    }

    // ==================================================================================
    //                      alter
    // ==================================================================================

    public abstract void hz_alter(long id, LongFunction f);

    public abstract Future<Void> hz_asyncAlter(long id, LongFunction f);

    @SectorOperation(cellbased = true)
    public void alter(LongCell cell, LongFunction f) {
        cell.value = f.apply(cell.value);
    }

}