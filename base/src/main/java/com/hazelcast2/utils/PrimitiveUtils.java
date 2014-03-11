package com.hazelcast2.utils;

public final class PrimitiveUtils {

    public static double longAsDouble(long value) {
        return Double.longBitsToDouble(value);
    }

    public static long doubleAsLong(double value) {
        return Double.doubleToLongBits(value);
    }

    private PrimitiveUtils(){}
}
