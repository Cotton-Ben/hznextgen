package com.hazelcast2.concurrent.atomicreference;

import com.hazelcast2.concurrent.atomiclong.impl.AtomicLongProxy;
import com.hazelcast2.concurrent.atomiclong.impl.GeneratedLongSector;
import com.hazelcast2.concurrent.atomicreference.impl.AtomicReferenceProxy;
import com.hazelcast2.concurrent.atomicreference.impl.GeneratedReferenceSector;
import com.hazelcast2.core.Hazelcast;
import com.hazelcast2.internal.instance.HazelcastInstanceImpl;
import com.hazelcast2.internal.nio.ByteArrayObjectDataInput;
import com.hazelcast2.serialization.SerializationService;
import com.hazelcast2.test.AssertTask;
import com.hazelcast2.test.HazelcastTestSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;

public class IAtomicReferenceDispatchTest extends HazelcastTestSupport {
    private HazelcastInstanceImpl hz;
    private SerializationService serializationService;

    @Before
    public void setUp() {
        hz = (HazelcastInstanceImpl) Hazelcast.newHazelcastInstance();
        serializationService = hz.getSerializationService();
        hz.startMaster();
    }

    @After
    public void tearDown() {
        hz.shutdown();
    }

    @Test
    public void testSet() throws IOException {
        final AtomicReferenceProxy ref = (AtomicReferenceProxy) hz.getAtomicReference(randomString());

        ByteBuffer b = ByteBuffer.allocate(1000);
        b.putShort(hz.getAtomicReferenceService().getServiceId());
        b.putInt(ref.getSector().getPartitionId());
        b.putShort(GeneratedReferenceSector.FUNCTION_doSet1);
        b.putLong(ref.getId());
        b.putLong(0);//call-id
        final String result = "foobar";
        b.put(serializationService.serialize(result));
        byte[] array = b.array();
        hz.dispatch(null,array);

        final ByteArrayObjectDataInput in = new ByteArrayObjectDataInput(array, 24, serializationService);
        Object x = in.readObject();
        System.out.println(x);

        assertTrueEventually(new AssertTask() {
            @Override
            public void run() throws Exception {
                assertEquals(result, ref.get());
            }
        });
    }

    @Test
    public void testoo() {
        final AtomicLongProxy atomicLong = (AtomicLongProxy) hz.getAtomicLong(randomString());

        ByteBuffer b = ByteBuffer.allocate(1000);
        b.putShort(hz.getAtomicReferenceService().getServiceId());
        b.putInt(atomicLong.getSector().getPartitionId());
        b.putShort(GeneratedLongSector.FUNCTION_doSet1);
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
