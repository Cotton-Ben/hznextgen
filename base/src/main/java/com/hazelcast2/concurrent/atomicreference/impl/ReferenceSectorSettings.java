package com.hazelcast2.concurrent.atomicreference.impl;

import com.hazelcast2.concurrent.atomicreference.impl.AtomicReferenceService;
import com.hazelcast2.spi.SectorSettings;

public class ReferenceSectorSettings extends SectorSettings {
    public AtomicReferenceService service;
}
