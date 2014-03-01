package com.hazelcast2.spi;

import org.junit.Test;

public class SegmentStressTest {

    @Test
    public void test() {
        LongSegment segment = new LongSegment(64);


    }

    private final class LongSegment extends Segment {
        private long counter;

        private LongSegment(int length) {
            super(length);
        }
    }

    public class StressThread extends Thread {
        private final LongSegment segment;

        public StressThread(LongSegment segment) {
            this.segment = segment;
        }

        public void run() {
            try {
                for (int k = 0; k < 1000 * 1000 * 100; k++) {
                    doRun();
                }
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }

        public void doRun() {
            final long seq = segment.claimSlot();
            if (seq == Segment.CLAIM_SLOT_LOCKED) {
                throw new UnsupportedOperationException();
            }

            //if(seq == Segment.CLAIM_SLOT_NO_CAPACITY)
        }
    }
}
