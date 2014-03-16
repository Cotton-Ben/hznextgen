package com.hazelcast2.internal.util;

import com.hazelcast2.internal.nio.IOUtils;
import org.junit.Test;

import java.nio.ByteBuffer;

import static com.hazelcast2.internal.nio.IOUtils.*;
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

//    @Test
//    public void testByte() {
//        byte[] buffer = new byte[1];
//
//        IOUtils.writeLong(update, buffer, 0);
//
//        long result = readLong(buffer, 0);
//
//        assertEquals(update, result);
//    }


    @Test
    public void testShort(){
        testShort((short) 0);
        testShort((short) -1);
        testShort((short) -100);
        testShort(Short.MAX_VALUE);
        testShort(Short.MAX_VALUE);
        testShort((short) 100);
        testShort((short) 1);
    }
    
    public void testShort(short value) {
        byte[] buffer = new byte[2];
        IOUtils.writeShort(value, buffer, 0);

        short result = IOUtils.readShort(buffer, 0);

        assertEquals(value, result);
    }

    @Test
    public void testBoolean(){
        testBoolean(true);
        testBoolean(false);
    }

    public void testBoolean(boolean value) {
        byte[] buffer = new byte[1];
        IOUtils.writeBoolean(value, buffer, 0);

        boolean result = IOUtils.readBoolean(buffer, 0);

        assertEquals(value, result);
    }

    @Test
    public void testLong() {
        testLong(0);
        testLong(-1);
        testLong(-100);
        testLong(Long.MAX_VALUE);
        testLong(Long.MAX_VALUE);
        testLong(100);
        testLong(1);
    }

    public void testLong(long value) {
        byte[] buffer = new byte[8];
        IOUtils.writeLong(value, buffer, 0);

        long result = IOUtils.readLong(buffer, 0);

        assertEquals(value, result);
    }

    @Test
    public void testInt() {
        testInt(0);
        testInt(-1);
        testInt(-100);
        testInt(Integer.MIN_VALUE);
        testInt(Integer.MAX_VALUE);
        testInt(100);
        testInt(1);
    }

    public void testInt(int value) {
        byte[] buffer = new byte[4];
        IOUtils.writeInt(value, buffer, 0);

        int result = IOUtils.readInt(buffer, 0);

        assertEquals(value, result);
    }

    @Test
    public void testDouble() {
        testDouble(0);
        testDouble(-1);
        testDouble(-100);
        testDouble(-Math.PI);
        testDouble(Double.MIN_VALUE);
        testDouble(Double.MAX_VALUE);
        testDouble(Math.PI);
        testDouble(100);
        testDouble(1);
    }

    public void testDouble(double value) {
        byte[] buffer = new byte[8];
        IOUtils.writeDouble(value, buffer, 0);

        double result = IOUtils.readDouble(buffer, 0);
        long valueLongBits = Double.doubleToLongBits(value);
        long resultLongBits = Double.doubleToLongBits(result);

        assertEquals(valueLongBits, resultLongBits);
    }

    @Test
    public void testFloat() {
        testFloat(0);
        testFloat(-1);
        testFloat(-100);
        testFloat(3.1415926f);
        testFloat(Float.MIN_VALUE);
        testFloat(Float.MAX_VALUE);
        testFloat(3.1415926f);
        testFloat(100);
        testFloat(1);
    }

    public void testFloat(float value) {
        byte[] buffer = new byte[4];
        IOUtils.writeFloat(value, buffer, 0);

        float result = IOUtils.readFloat(buffer, 0);
        int valueBits = Float.floatToIntBits(value);
        int resultBits = Float.floatToIntBits(result);

        assertEquals(valueBits, resultBits);
    }
}
