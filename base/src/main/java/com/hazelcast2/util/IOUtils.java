package com.hazelcast2.util;

public class IOUtils {

    public static short readShort(byte[] buffer, int offset) {
        byte high = buffer[offset];
        byte low = buffer[offset + 1];
        return (short) (((high & 0xFF) << 8) | (low & 0xFF));
    }

    public static long readLong(byte[] buffer, int position) {
        return (((long) buffer[position] << 56)
                + ((long) (buffer[position + 1] & 255) << 48)
                + ((long) (buffer[position + 2] & 255) << 40)
                + ((long) (buffer[position + 3] & 255) << 32)
                + ((long) (buffer[position + 4] & 255) << 24)
                + ((buffer[position + 5] & 255) << 16)
                + ((buffer[position + 6] & 255) << 8)
                + ((buffer[position + 7] & 255) << 0));

    }

    public static int readInt(byte[] buffer, int position) {
        return  + ((buffer[position + 0] & 255) << 24)
                + ((buffer[position + 1] & 255) << 16)
                + ((buffer[position + 2] & 255) << 8)
                + ((buffer[position + 3] & 255) << 0);
    }

    public static String readString(byte[] buffer, int position) {
        int size = readInt(buffer,position);
        return null;
    }
}
