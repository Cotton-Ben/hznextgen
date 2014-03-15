package com.hazelcast2.concurrent.lock;

import java.io.Serializable;

public class LockConfig implements Serializable {
    public String name;
    public byte backupCount = 1;
    public byte asyncBackupCount = 0;
}
