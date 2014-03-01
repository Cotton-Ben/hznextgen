package com.hazelcast2.map;

import com.hazelcast2.IMap;

public class MapProxy implements IMap {

    private MapPartition[] partitions;

    public MapProxy() {
        partitions = new MapPartition[5];
        for (int partitionId = 0; partitionId < partitions.length; partitionId++) {
        //    partitions[partitionId] = new GeneratedMapPartition(partitionId);
        }
    }

    @Override
    public String get(String key) {
        MapPartition partition = getPartition(key);
        return partition.doGet(key);
    }

    @Override
    public void set(String key, String value) {
        MapPartition partition = getPartition(key);
        partition.doSet(key, value);
    }

    public MapPartition getPartition(String key) {
        int partitionid = getPartitionid(key);
        return partitions[partitionid];
    }

    private int getPartitionid(String key) {
        int hash = key.hashCode();
        if (hash == Integer.MIN_VALUE) {
            hash = Integer.MAX_VALUE;
        } else if (hash < 0) {
            hash = -hash;
        }

        return hash % partitions.length;
    }
}
