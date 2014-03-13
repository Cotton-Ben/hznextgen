package com.hazelcast2.concurrent.atomiclong;

import com.hazelcast2.core.Hazelcast;
import com.hazelcast2.instance.HazelcastInstanceImpl;
import com.hazelcast2.test.AssertTask;
import com.hazelcast2.test.HazelcastTestSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;

public class IAtomicLongDispatchTest extends HazelcastTestSupport {
    private HazelcastInstanceImpl hz;

    @Before
    public void setUp() {
        hz = (HazelcastInstanceImpl) Hazelcast.newHazelcastInstance();
    }

    @After
    public void tearDown() {
        hz.shutdown();
    }

    @Test
    public void testInc() {
        final AtomicLongProxy atomicLong = (AtomicLongProxy) hz.getAtomicLong(randomString());

        ByteBuffer b = ByteBuffer.allocate(1000);
        b.putShort(hz.getAtomicLongService().getServiceId());
        b.putInt(atomicLong.getSector().getPartitionId());
        b.putShort(GeneratedLongSector.FUNCTION_doInc0);
        b.putLong(atomicLong.getId());

        byte[] array = b.array();
        hz.dispatch(array);

        assertTrueEventually(new AssertTask() {
            @Override
            public void run() throws Exception {
                assertEquals(1, atomicLong.get());
            }
        });
    }

    @Test
    public void testSet() {
        final AtomicLongProxy atomicLong = (AtomicLongProxy) hz.getAtomicLong(randomString());

        ByteBuffer b = ByteBuffer.allocate(1000);
        b.putShort(hz.getAtomicLongService().getServiceId());
        b.putInt(atomicLong.getSector().getPartitionId());
        b.putShort(GeneratedLongSector.FUNCTION_doSet1);
        b.putLong(atomicLong.getId());
        b.putLong(10);

        byte[] array = b.array();
        hz.dispatch(array);

        assertTrueEventually(new AssertTask() {
            @Override
            public void run() throws Exception {
                assertEquals(10, atomicLong.get());
            }
        });
    }
}
