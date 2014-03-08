package com.hazelcast2.spi;

import java.util.concurrent.atomic.AtomicLong;

/**
 * A SectorScheduler is responsible for scheduling sectors.
 * <p/>
 * The sector thread has a ringbuffer inside where sectors that need to be scheduled can be stored for scheduling.
 * <p/>
 * <p/>
 * The idea is that a sector can only be processed by a single thread. In the ideal situation, a thread that wants
 * to do a regular operation like a map.put or an atomiclong.get on a given sector, will try to to that himself to
 * prevent all the overload associated with offloading the work to another thread.
 * <p/>
 * But if a thread can't run a sector, it first needs to try to set the scheduled bit. If that succeeded, then
 * it can offload the sector to be scheduled by the sectorscheduler.
 * <p/>
 * The idea is that the sectorscheduler allows for workstealing. So if there is enough unprocessed work, and a
 * regular thread has nothing else to do then waiting (e.g. waiting for a remote call response), then it should
 * help out the sector scheduler. Should schedulers also help out each other? And should they be able to remove
 * a batch of work?
 * <p/>
 * Perhaps that in the (near) future the sector scheduler will be a general purpose structure where all work
 * can be processed, not only sectors.
 * <p/>
 * Another idea is to have multiple schedulers to reduce contention and each scheduler gets one dedicated thread.
 * <p/>
 * <p/>
 * http://stackoverflow.com/questions/9146855/determine-if-num-is-a-power-of-two-in-java
 */
public final class SectorScheduler {

    private final RingBuffer[] ringbuffers;
    private final int threadCount;
    private volatile boolean shutdown;

    public SectorScheduler(int size, int threadCount) {
        //todo: size needs to be a power of 2
        this.threadCount = threadCount;
        ringbuffers = new RingBuffer[threadCount];
        for (int k = 0; k < threadCount; k++) {
            ringbuffers[k] = new RingBuffer(size);
        }
    }

    public void start() {
        for (int k = 0; k < threadCount; k++) {
            RingBuffer ringBuffer = ringbuffers[k];
            SectorThread thread = new SectorThread(ringBuffer);
            thread.start();
        }
    }

    public void shutdown() {
        shutdown = true;
    }

    public void schedule(final Sector sector) {
        final RingBuffer ringBuffer = randomRingBuffer();
        final long seq = ringBuffer.claim();
        final SectorSlot slot = ringBuffer.getSlot(seq);
        slot.sector = sector;
        slot.publish(seq);
    }

    //todo: we need to come up with a better mechanism to find a ringbuffer.
    private RingBuffer randomRingBuffer() {
        return ringbuffers[0];
    }

    //todo: does this need to be padded?
    private static class SectorSlot {
        private Sector sector;
        private volatile long sequence = -1;

        public void publish(long sequence) {
            this.sequence = sequence;
        }

        public void awaitPublication(long seq) {
            while (sequence < seq) {
                Thread.yield();
            }
        }
    }

    private final class RingBuffer {
        private final AtomicLong prodSeq = new AtomicLong(0);
        private final AtomicLong consSeq = new AtomicLong(0);
        private final SectorSlot[] elements;
        private final int ringbufferSize;

        private RingBuffer(int ringBufferSize) {
            this.ringbufferSize = ringBufferSize;
            elements = new SectorSlot[ringBufferSize];

            for (int k = 0; k < ringBufferSize; k++) {
                elements[k] = new SectorSlot();
            }
        }

        private SectorSlot getSlot(final long sequence) {
            final int index = indexOf(sequence);
            return elements[index];
        }

        private long claim() {
            for (; ; ) {
                final long oldProdSeq = prodSeq.get();
                final long newProdSeq = oldProdSeq + 1;

                //todo: protection against overflow

                if (prodSeq.compareAndSet(oldProdSeq, newProdSeq)) {
                    return oldProdSeq;
                }
            }
        }

        private int indexOf(long sequence) {
            //todo: bitmagic.
            return (int) (sequence % ringbufferSize);
        }
    }

    private final class SectorThread extends Thread {
        private final RingBuffer ringbuffer;

        public SectorThread(RingBuffer ringBuffer) {
            this.ringbuffer = ringBuffer;
        }

        @Override
        public void run() {
            try {
                for (; ; ) {
                    doRun();
                }
            } catch (ShutdownException ignore) {

            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        private long claim() {
            final RingBuffer ringbuffer = this.ringbuffer;
            for (; ; ) {
                if (shutdown) {
                    throw new ShutdownException();
                }

                long c = ringbuffer.consSeq.get();
                if (ringbuffer.prodSeq.get() == c) {
                    Thread.yield();
                }

                return c;
            }
        }

        private void doRun() {
            final long seq = claim();
            final RingBuffer ringbuffer = this.ringbuffer;
            final SectorSlot slot = ringbuffer.getSlot(seq);
            slot.awaitPublication(seq);
            slot.sector.process();

            //todo:
            //increment and get is currently not yet needed, but in the future we'll have multiple consumers:
            //the sector-thread + threads that are helping out.
            ringbuffer.consSeq.incrementAndGet();
        }
    }

    private class ShutdownException extends RuntimeException {
    }
}
