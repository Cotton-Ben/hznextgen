package com.hazelcast2.concurrent.lock.impl;

import com.hazelcast2.concurrent.lock.LockConfig;

public class LockCell {
    public long lockOwnerThreadId = -1;
    public LockConfig config;
}
