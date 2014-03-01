package com.hazelcast2.util;

import com.hazelcast2.spi.PartitionSettings;

import java.lang.reflect.Constructor;

public class ReflectionUtils {

    public static <E> Constructor<E> getConstructor(String className) {
        try {
            Class constructor = ReflectionUtils.class.forName(className);
            return constructor.getConstructor(PartitionSettings.class);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
