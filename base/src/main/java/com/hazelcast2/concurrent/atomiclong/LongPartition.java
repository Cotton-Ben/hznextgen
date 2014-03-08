package com.hazelcast2.concurrent.atomiclong;

import com.hazelcast2.spi.Partition;
import com.hazelcast2.spi.PartitionSettings;
import com.hazelcast2.spi.cellbased.CellPartitionOperation;
import com.hazelcast2.spi.cellbased.CellBasedPartition;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

@CellBasedPartition
public abstract class LongPartition extends Partition {

    private final AtomicLong idGenerator = new AtomicLong();

    //todo: very inefficient structure.
    private final Map<Long, LongCell> cells = new HashMap<Long, LongCell>();

    public LongPartition(PartitionSettings partitionSettings) {
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

    @CellPartitionOperation
    public void set(LongCell cell, long update) {
        cell.value = update;
    }

    public abstract long doGet(long id);

    public abstract Future<Long> asyncDoGet(long id);

    @CellPartitionOperation
    public long get(LongCell cell) {
        return cell.value;
    }

    public abstract void doInc(long id);

    public abstract Future<Void> asyncDoInc(long id);

    @CellPartitionOperation
    public void inc(LongCell cell) {
        cell.value++;
    }
}