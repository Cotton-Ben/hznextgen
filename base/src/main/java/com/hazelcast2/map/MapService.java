package com.hazelcast2.map;

import com.hazelcast2.core.Config;
import com.hazelcast2.core.IMap;
import com.hazelcast2.core.MapConfig;
import com.hazelcast2.partition.PartitionService;
import com.hazelcast2.spi.SectorSettings;
import com.hazelcast2.spi.SectorScheduler;
import com.hazelcast2.spi.SpiService;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static com.hazelcast2.util.ReflectionUtils.getConstructor;

public class MapService implements SpiService {

    public static final String CLASS_NAME = "com.hazelcast2.map.GeneratedMapSector";

    private final PartitionService partitionService;
    private final Constructor<MapSector> constructor;
    private final SectorScheduler scheduler;
    private final Config config;
    private final short serviceId;

    public MapService(PartitionService partitionService, Config config, short serviceId) {
        this.partitionService = partitionService;
        this.constructor = getConstructor(CLASS_NAME,MapSectorSettings.class);
        this.scheduler = partitionService.getScheduler();
        this.config = config;
        this.serviceId = serviceId;
    }

    public IMap getDistributedObject(String name) {
        MapConfig mapConfig = config.getMapConfig(name);

        MapSector[] sectors = new MapSector[partitionService.getPartitionCount()];
        for (int partitionId = 0; partitionId < sectors.length; partitionId++) {
            MapSectorSettings settings = new MapSectorSettings();
            settings.partitionId = partitionId;
            settings.scheduler = scheduler;
            settings.serviceId = serviceId;
            settings.partitionService = partitionService;
            settings.mapConfig = mapConfig;
            try {
                sectors[partitionId] = constructor.newInstance(settings);
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
