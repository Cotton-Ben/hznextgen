package com.hazelcast2.spi;

import com.hazelcast2.concurrent.atomiclong.GeneratedLongPartition;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class AbstractPartitionTest {

    @Test
    public void lock_whenNotLocked() throws ExecutionException, InterruptedException {
        Partition partition = newPartition();
        Future f = partition.systemLock();
        assertNotNull(f);
        assertNull(f.get());
        assertTrue(partition.isSystemLocked());

        //for (int k = 0; k < partition.getSegmentLength(); k++) {
        //    Invocation invocation = partition.getInvocation(k);
        //    assertNotNull(invocation);
        //    assertTrue(invocation instanceof Partition.LockedInvocation);
        //}
    }

    @Test
    public void lock_whenAlreadyLocked() throws ExecutionException, InterruptedException {
        Partition partition = newPartition();
        partition.systemLock().get();

        try {
            partition.systemLock().get();
        } catch (ExecutionException e) {
            assertNotNull(e.getCause());
            assertTrue(e.getCause() instanceof IllegalStateException);
        }

        assertTrue(partition.isSystemLocked());
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
        Partition partition = newPartition();
        partition.systemLock().get();

        partition.systemUnlock();
        assertFalse(partition.isSystemLocked());

        //for (int k = 0; k < partition.getSegmentLength(); k++) {
        //    assertNull(partition.getInvocation(k));
        //}
    }

    private Partition newPartition() {
        return new GeneratedLongPartition(new PartitionSettings(1));
    }
}
