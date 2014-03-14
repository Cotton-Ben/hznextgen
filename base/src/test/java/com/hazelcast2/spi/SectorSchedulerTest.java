package com.hazelcast2.spi;

import org.junit.Test;

public class SectorSchedulerTest {

    @Test
    public void test(){
        SectorScheduler scheduler = new SectorScheduler(1024,1);
        Sector sector = new DummySector(new SectorSettings(1,scheduler));
        scheduler.schedule(sector);
    }

    class DummySector extends Sector{
        DummySector(SectorSettings sectorSettings) {
            super(sectorSettings);
        }

        @Override
        public void process() {

        }

        @Override
        public void schedule(InvocationEndpoint source, byte[] bytes) {
            throw new UnsupportedOperationException();
        }
    }
}
