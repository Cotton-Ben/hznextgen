package com.hazelcast2.internal.util;

import java.util.UUID;

public class StringUtils {

    public static String randomString() {
        return UUID.randomUUID().toString();
    }
}
