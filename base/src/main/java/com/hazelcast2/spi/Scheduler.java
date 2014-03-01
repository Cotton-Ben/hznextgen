package com.hazelcast2.spi;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Extremely inefficient implementation.
 */
public class Scheduler {

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
