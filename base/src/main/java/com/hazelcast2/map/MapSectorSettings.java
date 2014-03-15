package com.hazelcast2.map;

import com.hazelcast2.core.config.MapConfig;
import com.hazelcast2.spi.SectorSettings;

public class MapSectorSettings extends SectorSettings {
    public MapService mapService;
    public MapConfig mapConfig;
}
