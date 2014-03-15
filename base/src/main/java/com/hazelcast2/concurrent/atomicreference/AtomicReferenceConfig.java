package com.hazelcast2.concurrent.atomicreference;

import java.io.Serializable;

public class AtomicReferenceConfig implements Serializable{
    public String name;
    public byte backupCount = 1;
    public byte asyncBackupCount = 0;
}
