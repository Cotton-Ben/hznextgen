package com.hazelcast2.spi;

import com.hazelcast2.concurrent.atomiclong.GeneratedLongSector;
import com.hazelcast2.concurrent.atomiclong.LongSectorSettings;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@Ignore
public class AbstractSectorTest {

    @Test
    public void lock_whenNotLocked() throws ExecutionException, InterruptedException {
        Sector sector = newPartition();
        Future f = sector.systemLock();
        assertNotNull(f);
        assertNull(f.get());
        assertTrue(sector.isSystemLocked());

        //for (int k = 0; k < sector.getSegmentLength(); k++) {
        //    Invocation invocation = sector.getInvocation(k);
        //    assertNotNull(invocation);
        //    assertTrue(invocation instanceof Sector.LockedInvocation);
        //}
    }

    @Test
    public void lock_whenAlreadyLocked() throws ExecutionException, InterruptedException {
        Sector sector = newPartition();
        sector.systemLock().get();

        try {
            sector.systemLock().get();
        } catch (ExecutionException e) {
            assertNotNull(e.getCause());
            assertTrue(e.getCause() instanceof IllegalStateException);
        }

        assertTrue(sector.isSystemLocked());
    }

    @Test
    @Ignore
    public void lock_whenOperationRunning() {

    }

    @Test
    @Ignore
    public void lock_whenOperatingRunningAndPendingWork() {

    }

    @Test
    public void unlock_whenLocked() throws ExecutionException, InterruptedException {
        Sector sector = newPartition();
        sector.systemLock().get();

        sector.systemUnlock();
        assertFalse(sector.isSystemLocked());

        //for (int k = 0; k < sector.getSegmentLength(); k++) {
        //    assertNull(sector.getInvocation(k));
        //}
    }

    private Sector newPartition() {
        SectorScheduler sectorScheduler = new SectorScheduler(1024,1);
        LongSectorSettings settings = new LongSectorSettings();
        settings.partitionId = 1;
        settings.scheduler = sectorScheduler;
        return new GeneratedLongSector(settings);
    }
}
