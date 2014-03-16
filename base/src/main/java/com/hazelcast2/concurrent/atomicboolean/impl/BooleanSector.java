package com.hazelcast2.concurrent.atomicboolean.impl;

import com.hazelcast2.concurrent.atomicboolean.AtomicBooleanConfig;
import com.hazelcast2.core.IdNotFoundException;
import com.hazelcast2.spi.IdGenerator;
import com.hazelcast2.spi.Sector;
import com.hazelcast2.spi.SectorClass;
import com.hazelcast2.spi.SectorOperation;

import java.util.HashMap;
import java.util.concurrent.Future;

@SectorClass
public abstract class BooleanSector extends Sector {

    //todo: very inefficient structure.
    private final HashMap<Long, BooleanCell> cells = new HashMap<>();
    private final HashMap<String, Long> cellsId = new HashMap<>();

    private final IdGenerator idGenerator = new IdGenerator();

    public BooleanSector(BooleanSectorSettings sectorSettings) {
        super(sectorSettings);
    }

    public abstract long hz_createCell(AtomicBooleanConfig config);

    @SectorOperation
    public long createCell(AtomicBooleanConfig config) {
        Long found = cellsId.get(config.name);
        if (found != null) {
            return found;
        }

        BooleanCell cell = new BooleanCell();
        cell.config = config;
        long id = idGenerator.nextId();
        cells.put(id, cell);
        cellsId.put(config.name, id);
        return id;
    }

    public BooleanCell loadCell(long id) {
        BooleanCell cell = cells.get(id);
        if(cell == null){
           throw new IdNotFoundException("Can't find BooleanCell for id:"+id+" partition:"+partitionId+". " +
                   "It is very likely that the IAtomicBoolean has been destroyed.");
        }

        return cell;
    }

    // ==================================================================================
    //                      destroy
    // ==================================================================================

    public abstract void hz_destroy(long id);

    @SectorOperation
    public void destroy(long id) {
        BooleanCell cell = cells.remove(id);
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

    public abstract boolean hz_get(long id);

    public abstract Future<Boolean> hz_asyncGet(long id);

    @SectorOperation(readonly = true,cellbased = true)
    public boolean get(BooleanCell cell) {
        return cell.value;
    }

    // ==================================================================================
    //                      set
    // ==================================================================================

    public abstract boolean hz_set(long id, boolean update);

    public abstract Future<Boolean> hz_asyncSet(long id, boolean update);

    @SectorOperation(cellbased = true)
    public boolean set(BooleanCell cell, boolean update) {
        boolean oldValue = cell.value;
        cell.value = update;
        return oldValue;
    }

    // ==================================================================================
    //                      compareAndSet
    // ==================================================================================

    public abstract boolean hz_compareAndSet(long id, boolean old, boolean update);

    public abstract Future<Boolean> hz_asyncCompareAndSet(long id, boolean old, boolean update);

    @SectorOperation(cellbased = true)
    public boolean compareAndSet(BooleanCell cell, boolean old, boolean update) {
        if (cell.value != old) {
            return false;
        }

        cell.value = update;
        return true;
    }
}