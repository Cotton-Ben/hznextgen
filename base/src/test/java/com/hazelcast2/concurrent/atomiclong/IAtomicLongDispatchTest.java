package com.hazelcast2.concurrent.atomiclong;

import com.hazelcast2.concurrent.atomiclong.impl.AtomicLongProxy;
import com.hazelcast2.concurrent.atomiclong.impl.GeneratedLongSector;
import com.hazelcast2.core.Hazelcast;
import com.hazelcast2.internal.instance.HazelcastInstanceImpl;
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
        hz.startMaster();
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
        b.putShort(GeneratedLongSector.FUNCTION_hz_inc1);
        b.putLong(atomicLong.getId());
        b.putLong(Long.MIN_VALUE);//call-id

        byte[] array = b.array();
        hz.dispatch(null,array);

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
        b.putShort(GeneratedLongSector.FUNCTION_hz_set2);
        b.putLong(atomicLong.getId());
        b.putLong(Long.MIN_VALUE);//call-id
        b.putLong(10);

        byte[] array = b.array();
        hz.dispatch(null,array);

        assertTrueEventually(new AssertTask() {
            @Override
            public void run() throws Exception {
                assertEquals(10, atomicLong.get());
            }
        });
    }
}
