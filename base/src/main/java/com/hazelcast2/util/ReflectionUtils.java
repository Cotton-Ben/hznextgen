package com.hazelcast2.util;

import java.lang.reflect.Constructor;

public class ReflectionUtils {

    public static <E> Constructor<E> getConstructor(String className, Class... types) {
        try {
            Class constructor = ReflectionUtils.class.forName(className);
            return constructor.getConstructor(types);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
