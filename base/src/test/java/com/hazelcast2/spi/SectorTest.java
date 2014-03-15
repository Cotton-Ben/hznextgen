package com.hazelcast2.spi;

import com.hazelcast2.concurrent.atomiclong.impl.GeneratedLongSector;
import com.hazelcast2.concurrent.atomiclong.impl.LongSectorSettings;
import org.junit.Test;

import static org.junit.Assert.*;

public class SectorTest {


    // ========================== claimSlotAndReturnStatus ====================================

    @Test
    public void usingRingbufferForFewRounds() {
        Sector sector = generateSector(64);

        for (int k = 0; k < 1000; k++) {
            long seq = sector.claimSlotAndReturnStatus();
            assertTrue(Sector.isScheduled(seq));
            assertFalse(Sector.isLocked(seq));
            sector.conSeq.inc();
            assertTrue(sector.unschedule());
        }
    }

    public Sector generateSector(int ringBufferSize) {
        SectorScheduler sectorScheduler = new SectorScheduler(1024,1);
        LongSectorSettings settings = new LongSectorSettings();
        settings.partitionId = 1;
        settings.scheduler = sectorScheduler;
        settings.ringbufferSize = ringBufferSize;
        Sector sector = new GeneratedLongSector(settings);
        sector.unlock();
        return sector;
    }

    @Test
    public void claimSlot_whenOverloadDetected() {
        int length = 64;
        Sector sector = generateSector(length);

        for (int k = 0; k < length; k++) {
            long seq = sector.claimSlotAndReturnStatus();
            assertEquals(k, seq >> 2);
        }

        assertEquals(Sector.CLAIM_SLOT_NO_CAPACITY, sector.claimSlotAndReturnStatus());
    }

    @Test
    public void claimSlot_whenUnscheduled_thenSchedule() {
        Sector sector = generateSector(64);
        long initialSeq = sector.prodSeq.get();

        long seq = sector.claimSlotAndReturnStatus();

        assertTrue(Sector.isScheduled(seq));
        assertEquals(initialSeq + Sector.MASK_SCHEDULED, seq);
        assertEquals(initialSeq + Sector.DELTA + Sector.MASK_SCHEDULED, sector.prodSeq.get());
        assertFalse(sector.isLocked());
        assertTrue(sector.isScheduled());
        assertEquals(1, sector.size());
    }

    @Test
    public void claimSlot_whenLocked() {
        Sector sector = generateSector(64);
        sector.lock();
        long initialSeq = sector.prodSeq.get();

        long seq = sector.claimSlotAndReturnStatus();

        assertEquals(Sector.CLAIM_SLOT_REMOTE, seq);
        assertEquals(initialSeq, sector.prodSeq.get());
        assertTrue(sector.isLocked());
        assertFalse(sector.isScheduled());
        assertEquals(0, sector.size());
    }

    @Test
    public void claimSlot_whenScheduled() {
        Sector sector = generateSector(64);
        sector.claimSlotAndReturnStatus();
        long initialSeq = sector.prodSeq.get();

        long seq = sector.claimSlotAndReturnStatus();

        assertFalse(Sector.isScheduled(seq));
        assertEquals(initialSeq - Sector.MASK_SCHEDULED, seq);
        assertEquals(initialSeq + Sector.DELTA, sector.prodSeq.get());
        assertTrue(sector.isScheduled());
        assertFalse(sector.isLocked());
        assertEquals(2, sector.size());
    }

    @Test
    public void claimSlot_slotBeforeLast() {
        Sector sector = generateSector(3);
        long c1 = sector.claimSlotAndReturnStatus();
        long c2 = sector.claimSlotAndReturnStatus();

        long initialValue = sector.prodSeq.get();
        long result = sector.claimSlotAndReturnStatus();

        assertEquals(initialValue - Sector.MASK_SCHEDULED, result);
        //assertEquals(result, sector.prodSeq);
        assertEquals(3, sector.size());
    }

    @Test
    public void claimSlot_WhenOverloaded() {
        Sector sector = generateSector(2);
        sector.claimSlotAndReturnStatus();
        sector.claimSlotAndReturnStatus();

        long result = sector.claimSlotAndReturnStatus();
        assertEquals(Sector.CLAIM_SLOT_NO_CAPACITY, result);
        assertEquals(2, sector.size());
    }

    // ========================== unschedule ====================================

    @Test
    public void unschedule_whenUnscheduled() {
        Sector sector = generateSector(64);
        long initialSeq = sector.prodSeq.get();

        try {
            sector.unschedule();
            fail();
        } catch (IllegalStateException e) {

        }

        assertEquals(initialSeq, sector.prodSeq.get());
    }

    @Test
    public void unschedule_whenNoPendingWork() {
        Sector sector = generateSector(64);
        sector.claimSlotAndReturnStatus();
        sector.conSeq.inc(Sector.DELTA);

        long initialSeq = sector.prodSeq.get();

        boolean unscheduled = sector.unschedule();

        assertTrue(unscheduled);
        assertEquals(initialSeq - sector.MASK_SCHEDULED, sector.prodSeq.get());
    }

    @Test
    public void unschedule_whenNoPendingWorkAndLocked() {
        Sector sector = generateSector(64);
        sector.claimSlotAndReturnStatus();
        sector.lock();
        sector.conSeq.set(sector.prodSeq.get());

        long initialSeq = sector.prodSeq.get();

        boolean unscheduled = sector.unschedule();

        assertTrue(unscheduled);
        assertEquals(initialSeq - Sector.MASK_SCHEDULED, sector.prodSeq.get());
    }

    @Test
    public void unschedule_whenPendingWork() {
        Sector sector = generateSector(64);
        sector.claimSlotAndReturnStatus();

        long initialSeq = sector.prodSeq.get();

        boolean unscheduled = sector.unschedule();

        assertFalse(unscheduled);
        assertEquals(initialSeq, sector.prodSeq.get());
    }

    @Test
    public void unschedule_whenPendingWorkAndLocked() {
        Sector sector = generateSector(64);
        sector.claimSlotAndReturnStatus();
        sector.lock();

        long initialSeq = sector.prodSeq.get();

        boolean unscheduled = sector.unschedule();

        assertFalse(unscheduled);
        assertEquals(initialSeq, sector.prodSeq.get());
    }

    // ========================== lock ====================================

    @Test
    public void lock() {
        Sector sector = generateSector(64);
        long initialValue = sector.prodSeq.get();

        sector.lock();

        assertTrue(sector.isLocked());
        assertFalse(sector.isScheduled());
        assertEquals(initialValue + Sector.MASK_LOCKED, sector.prodSeq.get());
    }

    @Test
    public void lock_whenScheduled() {
        Sector sector = generateSector(64);
        sector.claimSlotAndReturnStatus();
        long prodSeq = sector.prodSeq.get();

        sector.lock();

        assertTrue(sector.isLocked());
        assertTrue(sector.isScheduled());
        assertEquals(prodSeq + Sector.MASK_LOCKED, sector.prodSeq.get());
    }

    @Test
    public void lock_whenAlreadyLocked() {
        Sector sector = generateSector(64);
        sector.lock();
        long prodSeq = sector.prodSeq.get();

        try {
            sector.lock();
            fail();
        } catch (IllegalStateException expected) {
        }

        assertEquals(prodSeq, sector.prodSeq.get());
        assertTrue(sector.isLocked());
        assertFalse(sector.isScheduled());
    }
}
