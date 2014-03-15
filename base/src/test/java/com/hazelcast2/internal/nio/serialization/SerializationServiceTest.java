package com.hazelcast2.internal.nio.serialization;

import com.hazelcast2.internal.nio.ByteArrayObjectDataInput;
import com.hazelcast2.internal.nio.ByteArrayObjectDataOutput;
import com.hazelcast2.serialization.SerializationService;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class SerializationServiceTest {

    private SerializationService serializationService;

    @Before
    public void setup() {
        serializationService = new SerializationService();
    }

    @Test
    public void test() {
        DummyObject o = new DummyObject();
        ByteArrayObjectDataOutput out = new ByteArrayObjectDataOutput(serializationService);
        serializationService.writeObject(out, o);
        ByteArrayObjectDataInput in = new ByteArrayObjectDataInput(out.toByteArray(), 0, serializationService);
        DummyObject found = serializationService.readObject(in);
        assertNotNull(found);
    }

    @Test
    public void testNull() {
        ByteArrayObjectDataOutput out = new ByteArrayObjectDataOutput(serializationService);
        serializationService.writeObject(out, null);
        ByteArrayObjectDataInput in = new ByteArrayObjectDataInput(out.toByteArray(), 0, serializationService);
        DummyObject found = serializationService.readObject(in);
        assertNull(found);
    }

    static class DummyObject implements Serializable {

    }
}
