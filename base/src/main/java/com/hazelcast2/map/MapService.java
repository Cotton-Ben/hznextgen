package com.hazelcast2.map;

import com.hazelcast2.concurrent.atomicboolean.BooleanSector;
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
    private final short serviceId;

    public MapService(SpiServiceSettings serviceSettings) {
        this.partitionService = serviceSettings.partitionService;
        this.serviceId = serviceSettings.serviceId;
        this.constructor = getConstructor(CLASS_NAME, MapSectorSettings.class);
        this.serviceSettings = serviceSettings;
        this.config = serviceSettings.config;
    }

    @Override
    public short getServiceId() {
        return serviceId;
    }

    public IMap getDistributedObject(String name) {
        MapConfig mapConfig = config.getMapConfig(name);

        MapSector[] sectors = new MapSector[partitionService.getPartitionCount()];
        for (int partitionId = 0; partitionId < sectors.length; partitionId++) {
            MapSectorSettings sectorSettings = new MapSectorSettings();
            sectorSettings.scheduler = partitionService.getScheduler();
            sectorSettings.partitionService = serviceSettings.partitionService;
            sectorSettings.serializationService = serviceSettings.serializationService;
            sectorSettings.serviceId = serviceSettings.serviceId;
            sectorSettings.partitionId = partitionId;
            sectorSettings.mapConfig = mapConfig;
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

    @Override
    public void enablePartition(int partitionId, boolean enable) {
        //BooleanSector sector = sectors[partitionId];
        //if (enable) {
        //    sector.lock();
        //} else {
        //    sector.unlock();
       // }
    }
}
