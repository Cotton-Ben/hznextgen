package com.hazelcast2.map.impl;

import com.hazelcast2.core.Config;
import com.hazelcast2.map.IMap;
import com.hazelcast2.map.MapConfig;
import com.hazelcast2.partition.PartitionService;
import com.hazelcast2.spi.InvocationEndpoint;
import com.hazelcast2.spi.PartitionAwareSpiService;
import com.hazelcast2.spi.SpiServiceSettings;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static com.hazelcast2.internal.util.ReflectionUtils.getConstructor;
import static com.hazelcast2.internal.util.StringUtils.randomString;

public class MapService implements PartitionAwareSpiService {

    public static final String CLASS_NAME = "com.hazelcast2.map.impl.GeneratedMapSector";

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

    public IMap getDistributedObject(MapConfig mapConfig) {
        if (mapConfig == null) {
            throw new NullPointerException("mapConfig can't be null");
        }

        if (mapConfig.name == null) {
            mapConfig.name = randomString();
        }

        MapSector[] sectors = new MapSector[partitionService.getPartitionCount()];
        for (int partitionId = 0; partitionId < sectors.length; partitionId++) {
            MapSectorSettings sectorSettings = new MapSectorSettings();
            sectorSettings.scheduler = partitionService.getScheduler();
            sectorSettings.partitionService = serviceSettings.partitionService;
            sectorSettings.serializationService = serviceSettings.serializationService;
            sectorSettings.invocationCompletionService = serviceSettings.invocationCompletionService;
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
        return new MapProxy(mapStore, -1, mapConfig.name);
    }

    @Override
    public void dispatch(final InvocationEndpoint source, byte[] invocationBytes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void enablePartition(int partitionId, boolean enable, InvocationEndpoint[] endpoints) {
        //BooleanSector sector = sectors[partitionId];
        //if (enable) {
        //    sector.lock();
        //} else {
        //    sector.unlock();
        // }
    }
}
