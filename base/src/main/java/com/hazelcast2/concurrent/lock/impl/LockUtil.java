package com.hazelcast2.concurrent.lock.impl;

public class LockUtil {

    private static final ThreadLocal<Long> threadLocal = new ThreadLocal<Long>();

    private LockUtil() {
    }

    public static long getThreadId() {
        final Long threadId = threadLocal.get();
        if (threadId != null) {
            return threadId;
        }
        return Thread.currentThread().getId();
    }

    public static void setThreadId(long threadId) {
        threadLocal.set(threadId);
    }

    public static void removeThreadId() {
        threadLocal.remove();
    }
}
