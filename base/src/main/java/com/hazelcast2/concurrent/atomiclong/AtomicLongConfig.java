package com.hazelcast2.concurrent.atomiclong;

import java.io.Serializable;

public class AtomicLongConfig implements Serializable {
    public String name;
    public byte backupCount = 1;
    public byte asyncBackupCount = 0;
}
