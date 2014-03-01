package com.hazelcast2.atomiclong;

public class AtomicLongService {

    private final LongPartition partition;

    public AtomicLongService(LongPartition partition) {
        this.partition = partition;
    }

    public AtomicLongProxy newProxy() {
        long id = partition.createCell();
        return new AtomicLongProxy(partition, id);
    }
}
