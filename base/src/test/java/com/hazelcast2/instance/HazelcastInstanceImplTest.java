package com.hazelcast2.instance;

import com.hazelcast2.core.config.Config;
import org.junit.Test;

public class HazelcastInstanceImplTest {

    @Test
    public void test(){
        HazelcastInstanceImpl hazelcastInstance = new HazelcastInstanceImpl(new Config());
    }
}
