package com.hazelcast2.util;

import com.hazelcast2.spi.SectorSettings;

import java.lang.reflect.Constructor;

public class ReflectionUtils {

    public static <E> Constructor<E> getConstructor(String className, Class... args) {
        Class[] types = new Class[1 + args.length];
        types[0] = SectorSettings.class;
        System.arraycopy(args, 0, types, 1, args.length);

        try {
            Class constructor = ReflectionUtils.class.forName(className);
            return constructor.getConstructor(types);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
