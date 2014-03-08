package com.hazelcast2.map;

import com.hazelcast2.core.IMap;
import com.hazelcast2.partition.PartitionService;
import com.hazelcast2.spi.PartitionSettings;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static com.hazelcast2.util.ReflectionUtils.getConstructor;

public class MapService {

    public static final String CLASS_NAME = "com.hazelcast2.concurrent.map.GeneratedMapPartition";

    private final PartitionService partitionService;
    private final Constructor<MapPartition> constructor = null;

    public MapService(PartitionService partitionService) {
        this.partitionService = partitionService;
        //constructor = getConstructor(CLASS_NAME);
    }

    public IMap getDistributedObject(String name) {
        if(true){
            return null;
        }
        MapPartition[] partitions = new MapPartition[partitionService.getPartitionCount()];
        for (int partitionId = 0; partitionId < partitions.length; partitionId++) {
            PartitionSettings partitionSettings = new PartitionSettings(partitionId);
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
