package com.hazelcast2.spi;

import org.junit.Test;

import static com.hazelcast2.spi.Segment.MASK_SCHEDULED;
import static com.hazelcast2.spi.Segment.isLocked;
import static com.hazelcast2.spi.Segment.isScheduled;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class SegmentTest {


    // ========================== claimSlot ====================================

    @Test
    public void usingRingbufferForFewRounds() {
        Segment segment = new Segment(64);

        for (int k = 0; k < 1000; k++) {
            long seq = segment.claimSlot();
            assertTrue(isScheduled(seq));
            assertFalse(isLocked(seq));
            segment.conSeq++;
            assertTrue(segment.unschedule());
        }
    }

    @Test
    public void claimSlot_whenOverloadDetected() {
        int length = 64;
        Segment segment = new Segment(length);

        for (int k = 0; k < length; k++) {
            long seq = segment.claimSlot();
            assertEquals(k, seq >> 2);
        }

        assertEquals(Segment.CLAIM_SLOT_NO_CAPACITY, segment.claimSlot());
    }

    @Test
    public void claimSlot_whenUnscheduled_thenSchedule() {
        Segment segment = new Segment(64);
        long initialSeq = segment.prodSeq;

        long seq = segment.claimSlot();

        assertTrue(isScheduled(seq));
        assertEquals(initialSeq + Segment.MASK_SCHEDULED, seq);
        assertEquals(initialSeq + Segment.DELTA + Segment.MASK_SCHEDULED, segment.prodSeq);
        assertFalse(segment.isLocked());
        assertTrue(segment.isScheduled());
        assertEquals(1, segment.size());
    }

    @Test
    public void claimSlot_whenLocked() {
        Segment segment = new Segment(64);
        segment.lock();
        long initialSeq = segment.prodSeq;

        long seq = segment.claimSlot();

        assertEquals(Segment.CLAIM_SLOT_LOCKED, seq);
        assertEquals(initialSeq, segment.prodSeq);
        assertTrue(segment.isLocked());
        assertFalse(segment.isScheduled());
        assertEquals(0, segment.size());
    }

    @Test
    public void claimSlot_whenScheduled() {
        Segment segment = new Segment(64);
        segment.claimSlot();
        long initialSeq = segment.prodSeq;

        long seq = segment.claimSlot();

        assertFalse(isScheduled(seq));
        assertEquals(initialSeq - Segment.MASK_SCHEDULED, seq);
        assertEquals(initialSeq + Segment.DELTA, segment.prodSeq);
        assertTrue(segment.isScheduled());
        assertFalse(segment.isLocked());
        assertEquals(2, segment.size());
    }

    @Test
    public void claimSlot_slotBeforeLast() {
        Segment segment = new Segment(3);
        long c1 = segment.claimSlot();
        long c2 = segment.claimSlot();

        long initialValue = segment.prodSeq;
        long result = segment.claimSlot();

        assertEquals(initialValue - MASK_SCHEDULED, result);
        //assertEquals(result, segment.prodSeq);
        assertEquals(3, segment.size());
    }

    @Test
    public void claimSlot_WhenOverloaded() {
        Segment segment = new Segment(2);
        segment.claimSlot();
        segment.claimSlot();

        long result = segment.claimSlot();
        assertEquals(Segment.CLAIM_SLOT_NO_CAPACITY, result);
        assertEquals(2, segment.size());
    }

    // ========================== unschedule ====================================

    @Test
    public void unschedule_whenUnscheduled() {
        Segment segment = new Segment(64);
        long initialSeq = segment.prodSeq;

        try {
            segment.unschedule();
            fail();
        } catch (IllegalStateException e) {

        }

        assertEquals(initialSeq, segment.prodSeq);
    }

    @Test
    public void unschedule_whenNoPendingWork() {
        Segment segment = new Segment(64);
        segment.claimSlot();
        segment.conSeq += Segment.DELTA;

        long initialSeq = segment.prodSeq;

        boolean unscheduled = segment.unschedule();

        assertTrue(unscheduled);
        assertEquals(initialSeq - MASK_SCHEDULED, segment.prodSeq);
    }

    @Test
    public void unschedule_whenNoPendingWorkAndLocked() {
        Segment segment = new Segment(64);
        segment.claimSlot();
        segment.lock();
        segment.conSeq = segment.prodSeq;

        long initialSeq = segment.prodSeq;

        boolean unscheduled = segment.unschedule();

        assertTrue(unscheduled);
        assertEquals(initialSeq - MASK_SCHEDULED, segment.prodSeq);
    }

    @Test
    public void unschedule_whenPendingWork() {
        Segment segment = new Segment(64);
        segment.claimSlot();

        long initialSeq = segment.prodSeq;

        boolean unscheduled = segment.unschedule();

        assertFalse(unscheduled);
        assertEquals(initialSeq, segment.prodSeq);
    }

    @Test
    public void unschedule_whenPendingWorkAndLocked() {
        Segment segment = new Segment(64);
        segment.claimSlot();
        segment.lock();

        long initialSeq = segment.prodSeq;

        boolean unscheduled = segment.unschedule();

        assertFalse(unscheduled);
        assertEquals(initialSeq, segment.prodSeq);
    }

    // ========================== lock ====================================

    @Test
    public void lock() {
        Segment segment = new Segment(64);
        long initialValue = segment.prodSeq;

        segment.lock();

        assertTrue(segment.isLocked());
        assertFalse(segment.isScheduled());
        assertEquals(initialValue + Segment.MASK_LOCKED, segment.prodSeq);
    }

    @Test
    public void lock_whenScheduled() {
        Segment segment = new Segment(64);
        segment.claimSlot();
        long prodSeq = segment.prodSeq;

        segment.lock();

        assertTrue(segment.isLocked());
        assertTrue(segment.isScheduled());
        assertEquals(prodSeq + Segment.MASK_LOCKED, segment.prodSeq);
    }

    @Test
    public void lock_whenAlreadyLocked() {
        Segment segment = new Segment(64);
        segment.lock();
        long prodSeq = segment.prodSeq;

        try {
            segment.lock();
            fail();
        } catch (IllegalStateException expected) {
        }

        assertEquals(prodSeq, segment.prodSeq);
        assertTrue(segment.isLocked());
        assertFalse(segment.isScheduled());
    }
}
