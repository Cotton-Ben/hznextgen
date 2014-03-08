package com.hazelcast2.spi;

import com.hazelcast2.concurrent.atomiclong.GeneratedLongSector;
import org.junit.Test;

import static org.junit.Assert.*;

public class SectorTest {


    // ========================== claimSlot ====================================

    @Test
    public void usingRingbufferForFewRounds() {
        Sector sector = generatePartition(64);

        for (int k = 0; k < 1000; k++) {
            long seq = sector.claimSlot();
            assertTrue(sector.isScheduled(seq));
            assertFalse(sector.isLocked(seq));
            sector.conSeq.inc();
            assertTrue(sector.unschedule());
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
        Sector sector = generatePartition(length);

        for (int k = 0; k < length; k++) {
            long seq = sector.claimSlot();
            assertEquals(k, seq >> 2);
        }

        assertEquals(sector.CLAIM_SLOT_NO_CAPACITY, sector.claimSlot());
    }

    @Test
    public void claimSlot_whenUnscheduled_thenSchedule() {
        Sector sector = generatePartition(64);
        long initialSeq = sector.prodSeq.get();

        long seq = sector.claimSlot();

        assertTrue(sector.isScheduled(seq));
        assertEquals(initialSeq + sector.MASK_SCHEDULED, seq);
        assertEquals(initialSeq + sector.DELTA + sector.MASK_SCHEDULED, sector.prodSeq);
        assertFalse(sector.isLocked());
        assertTrue(sector.isScheduled());
        assertEquals(1, sector.size());
    }

    @Test
    public void claimSlot_whenLocked() {
        Sector sector = generatePartition(64);
        sector.lock();
        long initialSeq = sector.prodSeq.get();

        long seq = sector.claimSlot();

        assertEquals(sector.CLAIM_SLOT_LOCKED, seq);
        assertEquals(initialSeq, sector.prodSeq);
        assertTrue(sector.isLocked());
        assertFalse(sector.isScheduled());
        assertEquals(0, sector.size());
    }

    @Test
    public void claimSlot_whenScheduled() {
        Sector sector = generatePartition(64);
        sector.claimSlot();
        long initialSeq = sector.prodSeq.get();

        long seq = sector.claimSlot();

        assertFalse(sector.isScheduled(seq));
        assertEquals(initialSeq - sector.MASK_SCHEDULED, seq);
        assertEquals(initialSeq + sector.DELTA, sector.prodSeq);
        assertTrue(sector.isScheduled());
        assertFalse(sector.isLocked());
        assertEquals(2, sector.size());
    }

    @Test
    public void claimSlot_slotBeforeLast() {
        Sector sector = generatePartition(3);
        long c1 = sector.claimSlot();
        long c2 = sector.claimSlot();

        long initialValue = sector.prodSeq.get();
        long result = sector.claimSlot();

        assertEquals(initialValue - sector.MASK_SCHEDULED, result);
        //assertEquals(result, sector.prodSeq);
        assertEquals(3, sector.size());
    }

    @Test
    public void claimSlot_WhenOverloaded() {
        Sector sector = generatePartition(2);
        sector.claimSlot();
        sector.claimSlot();

        long result = sector.claimSlot();
        assertEquals(sector.CLAIM_SLOT_NO_CAPACITY, result);
        assertEquals(2, sector.size());
    }

    // ========================== unschedule ====================================

    @Test
    public void unschedule_whenUnscheduled() {
        Sector sector = generatePartition(64);
        long initialSeq = sector.prodSeq.get();

        try {
            sector.unschedule();
            fail();
        } catch (IllegalStateException e) {

        }

        assertEquals(initialSeq, sector.prodSeq);
    }

    @Test
    public void unschedule_whenNoPendingWork() {
        Sector sector = generatePartition(64);
        sector.claimSlot();
        sector.conSeq.inc(sector.DELTA);

        long initialSeq = sector.prodSeq.get();

        boolean unscheduled = sector.unschedule();

        assertTrue(unscheduled);
        assertEquals(initialSeq - sector.MASK_SCHEDULED, sector.prodSeq);
    }

    @Test
    public void unschedule_whenNoPendingWorkAndLocked() {
        Sector sector = generatePartition(64);
        sector.claimSlot();
        sector.lock();
        sector.conSeq.set(sector.prodSeq.get());

        long initialSeq = sector.prodSeq.get();

        boolean unscheduled = sector.unschedule();

        assertTrue(unscheduled);
        assertEquals(initialSeq - sector.MASK_SCHEDULED, sector.prodSeq);
    }

    @Test
    public void unschedule_whenPendingWork() {
        Sector sector = generatePartition(64);
        sector.claimSlot();

        long initialSeq = sector.prodSeq.get();

        boolean unscheduled = sector.unschedule();

        assertFalse(unscheduled);
        assertEquals(initialSeq, sector.prodSeq);
    }

    @Test
    public void unschedule_whenPendingWorkAndLocked() {
        Sector sector = generatePartition(64);
        sector.claimSlot();
        sector.lock();

        long initialSeq = sector.prodSeq.get();

        boolean unscheduled = sector.unschedule();

        assertFalse(unscheduled);
        assertEquals(initialSeq, sector.prodSeq);
    }

    // ========================== lock ====================================

    @Test
    public void lock() {
        Sector sector = generatePartition(64);
        long initialValue = sector.prodSeq.get();

        sector.lock();

        assertTrue(sector.isLocked());
        assertFalse(sector.isScheduled());
        assertEquals(initialValue + sector.MASK_LOCKED, sector.prodSeq);
    }

    @Test
    public void lock_whenScheduled() {
        Sector sector = generatePartition(64);
        sector.claimSlot();
        long prodSeq = sector.prodSeq.get();

        sector.lock();

        assertTrue(sector.isLocked());
        assertTrue(sector.isScheduled());
        assertEquals(prodSeq + sector.MASK_LOCKED, sector.prodSeq);
    }

    @Test
    public void lock_whenAlreadyLocked() {
        Sector sector = generatePartition(64);
        sector.lock();
        long prodSeq = sector.prodSeq.get();

        try {
            sector.lock();
            fail();
        } catch (IllegalStateException expected) {
        }

        assertEquals(prodSeq, sector.prodSeq);
        assertTrue(sector.isLocked());
        assertFalse(sector.isScheduled());
    }
}
