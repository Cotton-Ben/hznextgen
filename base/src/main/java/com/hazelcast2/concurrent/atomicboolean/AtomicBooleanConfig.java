package com.hazelcast2.concurrent.atomicboolean;

import java.io.Serializable;

public class AtomicBooleanConfig implements Serializable {
    public String name;
    public byte backupCount = 1;
    public byte asyncBackupCount = 0;
}
