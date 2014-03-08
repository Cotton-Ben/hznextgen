package com.hazelcast2.map;

import com.hazelcast2.core.IMap;
import com.hazelcast2.partition.PartitionService;
import com.hazelcast2.spi.SectorScheduler;
import com.hazelcast2.spi.PartitionSettings;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class MapService {

    public static final String CLASS_NAME = "com.hazelcast2.concurrent.map.GeneratedMapSector";

    private final PartitionService partitionService;
    private final Constructor<MapSector> constructor = null;
    private final SectorScheduler scheduler;

    public MapService(PartitionService partitionService) {
        this.partitionService = partitionService;
        //constructor = getConstructor(CLASS_NAME);
        this.scheduler = partitionService.getScheduler();
    }

    public IMap getDistributedObject(String name) {
        if(true){
            return null;
        }

        MapSector[] partitions = new MapSector[partitionService.getPartitionCount()];
        for (int partitionId = 0; partitionId < partitions.length; partitionId++) {
            PartitionSettings partitionSettings = new PartitionSettings(partitionId,scheduler);
            partitionSettings.partitionService = partitionService;
            try {
                partitions[partitionId] = constructor.newInstance(partitionSettings);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        MapStore mapStore = new MapStore(partitions);
        return new MapProxy(mapStore);
    }
}
