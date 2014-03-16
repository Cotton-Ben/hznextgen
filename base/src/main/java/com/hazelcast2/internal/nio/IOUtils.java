package com.hazelcast2.internal.nio;

public class IOUtils {

    public static void writeBoolean(boolean update, byte[] buffer, int position) {
        buffer[position] = (byte) (update ? 1 : 0);
    }

    public static boolean readBoolean(byte[] buffer, int position) {
        return buffer[position] == 1;
    }

    public static byte readByte(byte[] buffer, int position) {
        return buffer[position];
    }

    public static void writeByte(byte update, byte[] buffer, int position) {
        buffer[position] = update;
    }

    public static short readShort(byte[] buffer, int position) {
        byte high = buffer[position];
        byte low = buffer[position + 1];
        return (short) (((high & 0xFF) << 8) | (low & 0xFF));
    }

    public static void writeShort(short v, byte[] buffer, int pos) {
        buffer[pos++] = (byte) (v >>> 8);
        buffer[pos] = (byte) (v);
    }

    public static char readChar(byte[] buffer, int position) {
        throw new RuntimeException();
    }

    public static void writeChar(char update, byte[] buffer, int position) {
        throw new RuntimeException();
    }

    public static int readInt(byte[] buffer, int position) {
        return +((buffer[position] & 255) << 24)
                + ((buffer[position + 1] & 255) << 16)
                + ((buffer[position + 2] & 255) << 8)
                + ((buffer[position + 3] & 255));
    }

    public static void writeInt(int update, byte[] buffer, int position) {
        buffer[position] = (byte) ((update >>> 24) & 255);
        buffer[position + 1] = (byte) ((update >>> 16) & 255);
        buffer[position + 2] = (byte) ((update >>> 8) & 255);
        buffer[position + 3] = (byte) ((update) & 255);
    }

    public static long readLong(byte[] buffer, int position) {
        return (((long) buffer[position] << 56)
                + ((long) (buffer[position + 1] & 255) << 48)
                + ((long) (buffer[position + 2] & 255) << 40)
                + ((long) (buffer[position + 3] & 255) << 32)
                + ((long) (buffer[position + 4] & 255) << 24)
                + ((buffer[position + 5] & 255) << 16)
                + ((buffer[position + 6] & 255) << 8)
                + ((buffer[position + 7] & 255)));

    }

    public static void writeLong(long v, byte[] buffer, int pos) {
        buffer[pos++] = (byte) (v >>> 56);
        buffer[pos++] = (byte) (v >>> 48);
        buffer[pos++] = (byte) (v >>> 40);
        buffer[pos++] = (byte) (v >>> 32);
        buffer[pos++] = (byte) (v >>> 24);
        buffer[pos++] = (byte) (v >>> 16);
        buffer[pos++] = (byte) (v >>> 8);
        buffer[pos] = (byte) (v);
    }

    public static void writeDouble(double update, byte[] buffer, int position) {
        long longBits = Double.doubleToLongBits(update);
        writeLong(longBits, buffer, position);
    }

    public static double readDouble(byte[] buffer, int position) {
        long longBits = readLong(buffer, position);
        return Double.longBitsToDouble(longBits);
    }

    public static void writeFloat(float update, byte[] buffer, int position) {
        int intBits = Float.floatToIntBits(update);
        writeInt(intBits, buffer, position);
    }

    public static float readFloat(byte[] buffer, int position) {
        int intBits = readInt(buffer, position);
        return Float.intBitsToFloat(intBits);
    }

    public static String readString(byte[] buffer, int position) {
        int size = readInt(buffer, position);
        throw new UnsupportedOperationException();
    }
}
