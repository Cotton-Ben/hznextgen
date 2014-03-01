package com.hazelcast2.spi;

import com.hazelcast2.concurrent.atomiclong.GeneratedLongPartition;
import org.junit.Test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class PartitionTest {


    // ========================== claimSlot ====================================

    @Test
    public void usingRingbufferForFewRounds() {
        Partition Partition = generatePartition(64);

        for (int k = 0; k < 1000; k++) {
            long seq = Partition.claimSlot();
            assertTrue(Partition.isScheduled(seq));
            assertFalse(Partition.isLocked(seq));
            Partition.conSeq++;
            assertTrue(Partition.unschedule());
        }
    }

    public Partition generatePartition(int ringBufferSize){
        PartitionSettings settings =new PartitionSettings(1);
        settings.ringbufferSize=ringBufferSize;
        return new GeneratedLongPartition(settings);
    }

    @Test
    public void claimSlot_whenOverloadDetected() {
        int length = 64;
        Partition Partition = generatePartition(length);

        for (int k = 0; k < length; k++) {
            long seq = Partition.claimSlot();
            assertEquals(k, seq >> 2);
        }

        assertEquals(Partition.CLAIM_SLOT_NO_CAPACITY, Partition.claimSlot());
    }

    @Test
    public void claimSlot_whenUnscheduled_thenSchedule() {
        Partition Partition = generatePartition(64);
        long initialSeq = Partition.prodSeq;

        long seq = Partition.claimSlot();

        assertTrue(Partition.isScheduled(seq));
        assertEquals(initialSeq + Partition.MASK_SCHEDULED, seq);
        assertEquals(initialSeq + Partition.DELTA + Partition.MASK_SCHEDULED, Partition.prodSeq);
        assertFalse(Partition.isLocked());
        assertTrue(Partition.isScheduled());
        assertEquals(1, Partition.size());
    }

    @Test
    public void claimSlot_whenLocked() {
        Partition Partition = generatePartition(64);
        Partition.lock();
        long initialSeq = Partition.prodSeq;

        long seq = Partition.claimSlot();

        assertEquals(Partition.CLAIM_SLOT_LOCKED, seq);
        assertEquals(initialSeq, Partition.prodSeq);
        assertTrue(Partition.isLocked());
        assertFalse(Partition.isScheduled());
        assertEquals(0, Partition.size());
    }

    @Test
    public void claimSlot_whenScheduled() {
        Partition Partition = generatePartition(64);
        Partition.claimSlot();
        long initialSeq = Partition.prodSeq;

        long seq = Partition.claimSlot();

        assertFalse(Partition.isScheduled(seq));
        assertEquals(initialSeq - Partition.MASK_SCHEDULED, seq);
        assertEquals(initialSeq + Partition.DELTA, Partition.prodSeq);
        assertTrue(Partition.isScheduled());
        assertFalse(Partition.isLocked());
        assertEquals(2, Partition.size());
    }

    @Test
    public void claimSlot_slotBeforeLast() {
        Partition Partition = generatePartition(3);
        long c1 = Partition.claimSlot();
        long c2 = Partition.claimSlot();

        long initialValue = Partition.prodSeq;
        long result = Partition.claimSlot();

        assertEquals(initialValue - Partition.MASK_SCHEDULED, result);
        //assertEquals(result, Partition.prodSeq);
        assertEquals(3, Partition.size());
    }

    @Test
    public void claimSlot_WhenOverloaded() {
        Partition Partition = generatePartition(2);
        Partition.claimSlot();
        Partition.claimSlot();

        long result = Partition.claimSlot();
        assertEquals(Partition.CLAIM_SLOT_NO_CAPACITY, result);
        assertEquals(2, Partition.size());
    }

    // ========================== unschedule ====================================

    @Test
    public void unschedule_whenUnscheduled() {
        Partition Partition = generatePartition(64);
        long initialSeq = Partition.prodSeq;

        try {
            Partition.unschedule();
            fail();
        } catch (IllegalStateException e) {

        }

        assertEquals(initialSeq, Partition.prodSeq);
    }

    @Test
    public void unschedule_whenNoPendingWork() {
        Partition Partition = generatePartition(64);
        Partition.claimSlot();
        Partition.conSeq += Partition.DELTA;

        long initialSeq = Partition.prodSeq;

        boolean unscheduled = Partition.unschedule();

        assertTrue(unscheduled);
        assertEquals(initialSeq - Partition.MASK_SCHEDULED, Partition.prodSeq);
    }

    @Test
    public void unschedule_whenNoPendingWorkAndLocked() {
        Partition Partition = generatePartition(64);
        Partition.claimSlot();
        Partition.lock();
        Partition.conSeq = Partition.prodSeq;

        long initialSeq = Partition.prodSeq;

        boolean unscheduled = Partition.unschedule();

        assertTrue(unscheduled);
        assertEquals(initialSeq - Partition.MASK_SCHEDULED, Partition.prodSeq);
    }

    @Test
    public void unschedule_whenPendingWork() {
        Partition Partition = generatePartition(64);
        Partition.claimSlot();

        long initialSeq = Partition.prodSeq;

        boolean unscheduled = Partition.unschedule();

        assertFalse(unscheduled);
        assertEquals(initialSeq, Partition.prodSeq);
    }

    @Test
    public void unschedule_whenPendingWorkAndLocked() {
        Partition Partition = generatePartition(64);
        Partition.claimSlot();
        Partition.lock();

        long initialSeq = Partition.prodSeq;

        boolean unscheduled = Partition.unschedule();

        assertFalse(unscheduled);
        assertEquals(initialSeq, Partition.prodSeq);
    }

    // ========================== lock ====================================

    @Test
    public void lock() {
        Partition Partition = generatePartition(64);
        long initialValue = Partition.prodSeq;

        Partition.lock();

        assertTrue(Partition.isLocked());
        assertFalse(Partition.isScheduled());
        assertEquals(initialValue + Partition.MASK_LOCKED, Partition.prodSeq);
    }

    @Test
    public void lock_whenScheduled() {
        Partition Partition = generatePartition(64);
        Partition.claimSlot();
        long prodSeq = Partition.prodSeq;

        Partition.lock();

        assertTrue(Partition.isLocked());
        assertTrue(Partition.isScheduled());
        assertEquals(prodSeq + Partition.MASK_LOCKED, Partition.prodSeq);
    }

    @Test
    public void lock_whenAlreadyLocked() {
        Partition Partition = generatePartition(64);
        Partition.lock();
        long prodSeq = Partition.prodSeq;

        try {
            Partition.lock();
            fail();
        } catch (IllegalStateException expected) {
        }

        assertEquals(prodSeq, Partition.prodSeq);
        assertTrue(Partition.isLocked());
        assertFalse(Partition.isScheduled());
    }
}
