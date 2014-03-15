package com.hazelcast2.internal.util;

import org.junit.Test;

import java.nio.ByteBuffer;

import static com.hazelcast2.internal.nio.IOUtils.readInt;
import static com.hazelcast2.internal.nio.IOUtils.readLong;
import static com.hazelcast2.internal.nio.IOUtils.readShort;
import static org.junit.Assert.assertEquals;

public class IOUtilsTest {

    @Test
    public void testReadShort() {
        ByteBuffer b = ByteBuffer.allocate(1000);
        short expect = 15000;
        b.putShort(expect);
        byte[] array = b.array();

        short result = readShort(array, 0);

        assertEquals(expect, result);
    }

    @Test
    public void testReadLong() {
        ByteBuffer b = ByteBuffer.allocate(1000);
        long expect = 15000;
        b.putLong(expect);
        byte[] array = b.array();

        long result = readLong(array, 0);

        assertEquals(expect, result);
    }

    @Test
    public void testReadInt() {
        ByteBuffer b = ByteBuffer.allocate(1000);
        int expect = 15000;
        b.putInt(expect);
        byte[] array = b.array();

        int result = readInt(array, 0);

        assertEquals(expect, result);
    }
}
