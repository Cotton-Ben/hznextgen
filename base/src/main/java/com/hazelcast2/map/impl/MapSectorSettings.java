package com.hazelcast2.map.impl;

import com.hazelcast2.map.MapConfig;
import com.hazelcast2.spi.SectorSettings;

public class MapSectorSettings extends SectorSettings {
    public MapService mapService;
    public MapConfig mapConfig;
}
