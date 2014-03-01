package com.hazelcast2.map;

public class MapStore {

    private final MapPartition[] partitions;

    public MapStore(int partitionCount){
        this.partitions = new MapPartition[partitionCount];
        //todo: partitions need to be made
    }

    public MapPartition getPartition(int partitionId){
        return partitions[partitionId];
    }

    public int getPartitionCount(){
        return partitions.length;
    }
}
