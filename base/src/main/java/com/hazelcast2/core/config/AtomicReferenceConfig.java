package com.hazelcast2.core.config;

import java.io.Serializable;

public class AtomicReferenceConfig implements Serializable{
    public String name;
    public byte backupCount = 1;
    public byte asyncBackupCount = 0;
}
