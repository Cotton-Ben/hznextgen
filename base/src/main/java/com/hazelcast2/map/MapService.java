package com.hazelcast2.map;

import com.hazelcast2.core.IMap;
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

    public MapService(PartitionService partitionService) {
        this.partitionService = partitionService;
        constructor = getConstructor(CLASS_NAME);
        this.scheduler = partitionService.getScheduler();
    }

    public IMap getDistributedObject(String name) {
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
        MapStore mapStore = new MapStore(sectors);
        return new MapProxy(mapStore);
    }
}
