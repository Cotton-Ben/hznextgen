package com.hazelcast2.map;

import com.hazelcast2.core.Config;
import com.hazelcast2.core.IMap;
import com.hazelcast2.core.MapConfig;
import com.hazelcast2.partition.PartitionService;
import com.hazelcast2.spi.SpiService;
import com.hazelcast2.spi.SpiServiceSettings;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static com.hazelcast2.util.ReflectionUtils.getConstructor;

public class MapService implements SpiService {

    public static final String CLASS_NAME = "com.hazelcast2.map.GeneratedMapSector";

    private final PartitionService partitionService;
    private final Constructor<MapSector> constructor;
    private final Config config;
    private final SpiServiceSettings serviceSettings;

    public MapService(SpiServiceSettings sectorSettings) {
        this.partitionService = sectorSettings.partitionService;
        this.constructor = getConstructor(CLASS_NAME, MapSectorSettings.class);
        this.serviceSettings = sectorSettings;
        this.config = sectorSettings.config;
    }

    public IMap getDistributedObject(String name) {
        MapConfig mapConfig = config.getMapConfig(name);

        MapSector[] sectors = new MapSector[partitionService.getPartitionCount()];
        for (int partitionId = 0; partitionId < sectors.length; partitionId++) {
            MapSectorSettings sectorSettings = new MapSectorSettings();
            sectorSettings.partitionId = partitionId;
            sectorSettings.scheduler = partitionService.getScheduler();
            sectorSettings.serviceId = serviceSettings.serviceId;
            sectorSettings.partitionService = partitionService;
            sectorSettings.mapConfig = mapConfig;
            sectorSettings.serializationService = serviceSettings.serializationService;
            try {
                sectors[partitionId] = constructor.newInstance(sectorSettings);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        MapStore mapStore = new MapStore(sectors, mapConfig);
        return new MapProxy(mapStore);
    }

    @Override
    public void schedule(byte[] invocationBytes) {
        throw new UnsupportedOperationException();
    }
}
