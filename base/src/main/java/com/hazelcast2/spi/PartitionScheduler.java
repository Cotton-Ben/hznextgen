package com.hazelcast2.spi;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Extremely inefficient implementation because lots of locking and object creation.
 * Needs to be replaced with a ringbuffer and should be capable of letting threads
 * that can't do anything else than blocking, to help out so they don't need to block.
 */
public class PartitionScheduler {

    private final Executor executor = Executors.newFixedThreadPool(8);

    public void schedule(final Partition partition) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    partition.process();
                } catch (RuntimeException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
}
