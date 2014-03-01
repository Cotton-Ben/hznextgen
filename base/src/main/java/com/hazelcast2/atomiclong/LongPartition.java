package com.hazelcast2.atomiclong;

import com.hazelcast2.spi.AbstractPartition;
import com.hazelcast2.spi.PartitionSettings;
import com.hazelcast2.spi.Segment;
import com.hazelcast2.spi.OperationMethod;
import com.hazelcast2.spi.PartitionAnnotation;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

@PartitionAnnotation
public abstract class LongPartition extends AbstractPartition {

    private final AtomicLong idGenerator = new AtomicLong();

    public LongPartition(PartitionSettings partitionSettings) {
        super(partitionSettings);
    }

    @Override
    public Segment createSegment() {
        return new LongSegment(64);
    }

    public long createCell() {
        LongCell cell = new LongCell();
        long id = idGenerator.incrementAndGet();
        int segmentIndex = getSegmentIndex(id);
        LongSegment segment = (LongSegment) getSegment(segmentIndex);
        segment.cells.put(id, cell);
        return id;
    }

     public LongCell loadCell(long id) {
        int segmentIndex = getSegmentIndex(id);
         LongSegment segment = (LongSegment)getSegment(segmentIndex);
        return segment.cells.get(id);
    }

    public abstract void doSet(long id, long update);

    public abstract Future<Void> asyncDoSet(long id, long update);

    @OperationMethod
    public void set(LongCell cell, long update) {
        cell.value = update;
    }

    public abstract long doGet(long id);

    public abstract Future<Long> asyncDoGet(long id);

    @OperationMethod
    public long get(LongCell cell) {
        return cell.value;
    }

    public abstract void doInc(long id);

    public abstract Future<Void> asyncDoInc(long id);

    @OperationMethod
    public void inc(LongCell cell) {
        cell.value++;
    }

    private static class LongSegment extends Segment {
        //very inefficient structure.
        public final Map<Long, LongCell> cells = new HashMap<Long, LongCell>();

        private LongSegment(int length) {
            super(length);
        }
    }
}