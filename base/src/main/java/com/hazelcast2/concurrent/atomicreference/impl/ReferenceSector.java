package com.hazelcast2.concurrent.atomicreference.impl;

import com.hazelcast2.concurrent.atomicreference.AtomicReferenceConfig;
import com.hazelcast2.core.IFunction;
import com.hazelcast2.core.IdNotFoundException;
import com.hazelcast2.spi.IdGenerator;
import com.hazelcast2.spi.Sector;
import com.hazelcast2.spi.SectorClass;
import com.hazelcast2.spi.SectorOperation;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

@SectorClass
public abstract class ReferenceSector extends Sector {

    private final IdGenerator idGenerator = new IdGenerator();

    //todo: very inefficient structure.
    private final Map<Long, ReferenceCell> cells = new HashMap<>();
    private final HashMap<String, Long> cellsId = new HashMap<>();

    public ReferenceSector(ReferenceSectorSettings settings) {
        super(settings);
    }

    public abstract long hz_createCell(AtomicReferenceConfig config);

    @SectorOperation
    public long createCell(AtomicReferenceConfig config) {
        Long found = cellsId.get(config.name);
        if (found != null) {
            return found;
        }

        ReferenceCell cell = new ReferenceCell();
        cell.config = config;
        long id = idGenerator.nextId();
        cells.put(id, cell);
        cellsId.put(config.name, id);
        return id;
    }

    public ReferenceCell loadCell(long id) {
        ReferenceCell cell = cells.get(id);
        if (cell == null) {
            throw new IdNotFoundException("Can't find ReferenceCell for id:" + id + " partition:" + partitionId + ". " +
                    "It is very likely that the IAtomicReference has been destroyed.");
        }

        return cell;
    }

    // ==================================================================================
    //                      destroy
    // ==================================================================================

    public abstract void hz_destroy(long id);

    @SectorOperation
    public void destroy(long id) {
        ReferenceCell cell = cells.remove(id);
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

    // ==================== get ================================================

    public abstract Object hz_get(long id);

    public abstract Future<Object> hz_asyncGet(long id);

    @SectorOperation(cellbased = true, readonly = true)
    public Object get(ReferenceCell cell) {
        return cell.value;
    }

    // ==================== isNull ================================================

    public abstract boolean hz_isNull(long id);

    public abstract Future<Boolean> hz_asyncIsNull(long id);

    @SectorOperation(cellbased = true, readonly = true)
    public boolean isNull(ReferenceCell cell) {
        return cell.value == null;
    }

    // ==================== set ================================================

    public abstract void hz_set(long id, Object update);

    public abstract Future<Void> hz_asyncSet(long id, Object update);

    @SectorOperation(cellbased = true)
    public void set(ReferenceCell cell, Object update) {
        cell.value = update;
    }

    // ==================== compare and set =====================================

    public abstract boolean hz_compareAndSet(long id, Object expect, Object update);

    public abstract Future<Boolean> hz_asyncCompareAndSet(long id, Object expect, Object update);

    @SectorOperation(cellbased = true)
    public boolean compareAndSet(ReferenceCell cell, Object expect, Object update) {
        if (cell.value != expect) {
            return false;
        }

        cell.value = update;
        return true;
    }

    // ==================== apply =====================================

    public abstract Object hz_apply(long id, IFunction f);

    public abstract Object hz_asyncApply(long id, IFunction f);

    @SectorOperation(cellbased = true, readonly = true)
    public Object apply(ReferenceCell cell, IFunction f) {
        return f.apply(cell.value);
    }
}