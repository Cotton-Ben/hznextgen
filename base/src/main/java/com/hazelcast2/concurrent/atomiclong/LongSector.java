package com.hazelcast2.concurrent.atomiclong;

import com.hazelcast2.core.LongFunction;
import com.hazelcast2.spi.PartitionSettings;
import com.hazelcast2.spi.Sector;
import com.hazelcast2.spi.cellbased.CellBasedSector;
import com.hazelcast2.spi.cellbased.CellSectorOperation;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

@CellBasedSector
public abstract class LongSector extends Sector {

    private final AtomicLong idGenerator = new AtomicLong();

    //todo: very inefficient structure.
    private final Map<Long, LongCell> cells = new HashMap<Long, LongCell>();

    public LongSector(PartitionSettings partitionSettings) {
        super(partitionSettings);
    }

    public long createCell() {
        LongCell cell = new LongCell();
        long id = idGenerator.incrementAndGet();
        cells.put(id, cell);
        return id;
    }

    public LongCell loadCell(long id) {
        return cells.get(id);
    }

    public abstract void doSet(long id, long update);

    public abstract Future<Void> asyncDoSet(long id, long update);

    @CellSectorOperation
    public void set(LongCell cell, long update) {
        cell.value = update;
    }

    public abstract long doGet(long id);

    public abstract Future<Long> asyncDoGet(long id);

    @CellSectorOperation
    public long get(LongCell cell) {
        return cell.value;
    }

    public abstract void doInc(long id);

    public abstract Future<Void> asyncDoInc(long id);

    @CellSectorOperation
    public void inc(LongCell cell) {
        cell.value++;
    }

    public abstract boolean doCompareAndSet(long id, long expect, long update);

    public abstract Future<Boolean> asyncDoCompareAndSet(long id, long expect, long update);

    @CellSectorOperation
    public boolean compareAndSet(LongCell cell, long expect, long update) {
        if (cell.value != expect) {
            return false;
        }

        cell.value = update;
        return true;
    }

    public abstract long doApply(long id, LongFunction f);

    public abstract Future<Long> asyncDoApply(long id, LongFunction f);

    @CellSectorOperation
    public long apply(LongCell cell, LongFunction f) {
        return f.apply(cell.value);
    }
}