package com.hazelcast2.atomicboolean;


import com.hazelcast2.spi.Partition;
import com.hazelcast2.spi.OperationMethod;
import com.hazelcast2.spi.PartitionAnnotation;
import com.hazelcast2.spi.PartitionSettings;

import java.util.HashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

@PartitionAnnotation
public abstract class BooleanPartition extends Partition {
    private final HashMap<Long, BooleanCell> cells = new HashMap<>();

    private final AtomicLong idGenerator = new AtomicLong();

    public BooleanPartition(PartitionSettings partitionSettings) {
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

    @OperationMethod
    public boolean set(BooleanCell cell, boolean update) {
        boolean oldValue = cell.value;
        cell.value = update;
        return oldValue;
    }

    // ================== get ============================================================

    public abstract boolean doGet(long id);

    public abstract Future<Boolean> asyncDoGet(long id);

    @OperationMethod
    public boolean get(BooleanCell cell) {
        return cell.value;
    }

    // ================== compareAndSet =================================================

    public abstract boolean doCompareAndSet(long id, boolean old, boolean update);

    public abstract Future<Boolean> asyncDoCompareAndSet(long id, boolean old, boolean update);

    @OperationMethod
    public boolean compareAndSet(BooleanCell cell, boolean old, boolean update) {
        if(cell.value!=old){
            return false;
        }

        cell.value = update;
        return true;
    }

}