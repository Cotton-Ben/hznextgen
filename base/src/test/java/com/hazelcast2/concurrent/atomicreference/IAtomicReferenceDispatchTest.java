package com.hazelcast2.concurrent.atomicreference;

import com.hazelcast2.concurrent.atomicreference.impl.AtomicReferenceProxy;
import com.hazelcast2.concurrent.atomicreference.impl.GeneratedReferenceSector;
import com.hazelcast2.core.Hazelcast;
import com.hazelcast2.internal.instance.HazelcastInstanceImpl;
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
        b.putShort(GeneratedReferenceSector.FUNCTION_hz_set2);
        b.putLong(ref.getId());
        b.putLong(0);//call-id
        final String result = "foobar";
        b.put(serializationService.serialize(result));
        byte[] array = b.array();
        hz.dispatch(null, array);

        assertTrueEventually(new AssertTask() {
            @Override
            public void run() throws Exception {
                assertEquals(result, ref.get());
            }
        });
    }
}
