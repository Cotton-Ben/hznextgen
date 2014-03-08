package com.hazelcast2.spi;

import com.hazelcast2.concurrent.atomiclong.GeneratedLongSector;
import org.junit.Test;

import static org.junit.Assert.*;

public class SectorTest {


    // ========================== claimSlot ====================================

    @Test
    public void usingRingbufferForFewRounds() {
        Sector Sector = generatePartition(64);

        for (int k = 0; k < 1000; k++) {
            long seq = Sector.claimSlot();
            assertTrue(Sector.isScheduled(seq));
            assertFalse(Sector.isLocked(seq));
            Sector.conSeq++;
            assertTrue(Sector.unschedule());
        }
    }

    public Sector generatePartition(int ringBufferSize) {
        SectorScheduler sectorScheduler = new SectorScheduler(1024,1);
        PartitionSettings settings = new PartitionSettings(1, sectorScheduler);
        settings.ringbufferSize = ringBufferSize;
        return new GeneratedLongSector(settings);
    }

    @Test
    public void claimSlot_whenOverloadDetected() {
        int length = 64;
        Sector Sector = generatePartition(length);

        for (int k = 0; k < length; k++) {
            long seq = Sector.claimSlot();
            assertEquals(k, seq >> 2);
        }

        assertEquals(Sector.CLAIM_SLOT_NO_CAPACITY, Sector.claimSlot());
    }

    @Test
    public void claimSlot_whenUnscheduled_thenSchedule() {
        Sector Sector = generatePartition(64);
        long initialSeq = Sector.prodSeq;

        long seq = Sector.claimSlot();

        assertTrue(Sector.isScheduled(seq));
        assertEquals(initialSeq + Sector.MASK_SCHEDULED, seq);
        assertEquals(initialSeq + Sector.DELTA + Sector.MASK_SCHEDULED, Sector.prodSeq);
        assertFalse(Sector.isLocked());
        assertTrue(Sector.isScheduled());
        assertEquals(1, Sector.size());
    }

    @Test
    public void claimSlot_whenLocked() {
        Sector Sector = generatePartition(64);
        Sector.lock();
        long initialSeq = Sector.prodSeq;

        long seq = Sector.claimSlot();

        assertEquals(Sector.CLAIM_SLOT_LOCKED, seq);
        assertEquals(initialSeq, Sector.prodSeq);
        assertTrue(Sector.isLocked());
        assertFalse(Sector.isScheduled());
        assertEquals(0, Sector.size());
    }

    @Test
    public void claimSlot_whenScheduled() {
        Sector Sector = generatePartition(64);
        Sector.claimSlot();
        long initialSeq = Sector.prodSeq;

        long seq = Sector.claimSlot();

        assertFalse(Sector.isScheduled(seq));
        assertEquals(initialSeq - Sector.MASK_SCHEDULED, seq);
        assertEquals(initialSeq + Sector.DELTA, Sector.prodSeq);
        assertTrue(Sector.isScheduled());
        assertFalse(Sector.isLocked());
        assertEquals(2, Sector.size());
    }

    @Test
    public void claimSlot_slotBeforeLast() {
        Sector Sector = generatePartition(3);
        long c1 = Sector.claimSlot();
        long c2 = Sector.claimSlot();

        long initialValue = Sector.prodSeq;
        long result = Sector.claimSlot();

        assertEquals(initialValue - Sector.MASK_SCHEDULED, result);
        //assertEquals(result, Sector.prodSeq);
        assertEquals(3, Sector.size());
    }

    @Test
    public void claimSlot_WhenOverloaded() {
        Sector Sector = generatePartition(2);
        Sector.claimSlot();
        Sector.claimSlot();

        long result = Sector.claimSlot();
        assertEquals(Sector.CLAIM_SLOT_NO_CAPACITY, result);
        assertEquals(2, Sector.size());
    }

    // ========================== unschedule ====================================

    @Test
    public void unschedule_whenUnscheduled() {
        Sector Sector = generatePartition(64);
        long initialSeq = Sector.prodSeq;

        try {
            Sector.unschedule();
            fail();
        } catch (IllegalStateException e) {

        }

        assertEquals(initialSeq, Sector.prodSeq);
    }

    @Test
    public void unschedule_whenNoPendingWork() {
        Sector Sector = generatePartition(64);
        Sector.claimSlot();
        Sector.conSeq += Sector.DELTA;

        long initialSeq = Sector.prodSeq;

        boolean unscheduled = Sector.unschedule();

        assertTrue(unscheduled);
        assertEquals(initialSeq - Sector.MASK_SCHEDULED, Sector.prodSeq);
    }

    @Test
    public void unschedule_whenNoPendingWorkAndLocked() {
        Sector Sector = generatePartition(64);
        Sector.claimSlot();
        Sector.lock();
        Sector.conSeq = Sector.prodSeq;

        long initialSeq = Sector.prodSeq;

        boolean unscheduled = Sector.unschedule();

        assertTrue(unscheduled);
        assertEquals(initialSeq - Sector.MASK_SCHEDULED, Sector.prodSeq);
    }

    @Test
    public void unschedule_whenPendingWork() {
        Sector Sector = generatePartition(64);
        Sector.claimSlot();

        long initialSeq = Sector.prodSeq;

        boolean unscheduled = Sector.unschedule();

        assertFalse(unscheduled);
        assertEquals(initialSeq, Sector.prodSeq);
    }

    @Test
    public void unschedule_whenPendingWorkAndLocked() {
        Sector Sector = generatePartition(64);
        Sector.claimSlot();
        Sector.lock();

        long initialSeq = Sector.prodSeq;

        boolean unscheduled = Sector.unschedule();

        assertFalse(unscheduled);
        assertEquals(initialSeq, Sector.prodSeq);
    }

    // ========================== lock ====================================

    @Test
    public void lock() {
        Sector Sector = generatePartition(64);
        long initialValue = Sector.prodSeq;

        Sector.lock();

        assertTrue(Sector.isLocked());
        assertFalse(Sector.isScheduled());
        assertEquals(initialValue + Sector.MASK_LOCKED, Sector.prodSeq);
    }

    @Test
    public void lock_whenScheduled() {
        Sector Sector = generatePartition(64);
        Sector.claimSlot();
        long prodSeq = Sector.prodSeq;

        Sector.lock();

        assertTrue(Sector.isLocked());
        assertTrue(Sector.isScheduled());
        assertEquals(prodSeq + Sector.MASK_LOCKED, Sector.prodSeq);
    }

    @Test
    public void lock_whenAlreadyLocked() {
        Sector Sector = generatePartition(64);
        Sector.lock();
        long prodSeq = Sector.prodSeq;

        try {
            Sector.lock();
            fail();
        } catch (IllegalStateException expected) {
        }

        assertEquals(prodSeq, Sector.prodSeq);
        assertTrue(Sector.isLocked());
        assertFalse(Sector.isScheduled());
    }
}
