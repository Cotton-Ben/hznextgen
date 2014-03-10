package com.hazelcast2.map;

import com.hazelcast2.core.Config;
import com.hazelcast2.core.IMap;
import com.hazelcast2.core.MapConfig;
import com.hazelcast2.partition.PartitionService;
import com.hazelcast2.spi.PartitionSettings;
import com.hazelcast2.spi.SectorScheduler;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static com.hazelcast2.util.ReflectionUtils.getConstructor;

public class MapService {

    public static final String CLASS_NAME = "com.hazelcast2.map.GeneratedMapSector";

    private final PartitionService partitionService;
    private final Constructor<MapSector> constructor;
    private final SectorScheduler scheduler;
    private final Config config;

    public MapService(PartitionService partitionService, Config config) {
        this.partitionService = partitionService;
        this.constructor = getConstructor(CLASS_NAME);
        this.scheduler = partitionService.getScheduler();
        this.config = config;
    }

    public IMap getDistributedObject(String name) {
        MapConfig mapConfig = config.getMapConfig(name);

        MapSector[] sectors = new MapSector[partitionService.getPartitionCount()];
        for (int partitionId = 0; partitionId < sectors.length; partitionId++) {
            PartitionSettings partitionSettings = new PartitionSettings(partitionId, scheduler);
            partitionSettings.partitionService = partitionService;
            try {
                sectors[partitionId] = constructor.newInstance(partitionSettings);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        MapStore mapStore = new MapStore(sectors, mapConfig);
        return new MapProxy(mapStore);
    }
}
