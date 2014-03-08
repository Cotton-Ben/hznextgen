package com.hazelcast2.spi;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Extremely inefficient implementation because lots of locking and object creation.
 * Needs to be replaced with a ringbuffer and should be capable of letting threads
 * that can't do anything else than blocking, to help out so they don't need to block.
 *
 *
 */
public class SectorScheduler {

    private final Executor executor = Executors.newFixedThreadPool(8);
    private final SectorSlot[] ringbuffer;
    //private final AtomicLong

    public SectorScheduler(int size) {
        this.ringbuffer = new SectorSlot[size];

        for (int k = 0; k < size; k++) {
            ringbuffer[k] = new SectorSlot();
        }
    }

    public void schedule(final Sector sector) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    sector.process();
                } catch (RuntimeException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private static class SectorSlot {
        private Sector sector;
        private int sequence = -1;


    }
}
