package com.hazelcast2.spi;

import org.junit.Test;

public class SectorSchedulerTest {

    @Test
    public void test(){
        SectorScheduler scheduler = new SectorScheduler(1024,1);
        Sector sector = new DummySector(new PartitionSettings(1,scheduler));
        scheduler.schedule(sector);


    }

    class DummySector extends Sector{
        DummySector(PartitionSettings partitionSettings) {
            super(partitionSettings);
        }

        @Override
        public void process() {

        }
    }
}
