package com.hazelcast2.internal.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SequenceTest {

    @Test
    public void test(){
        Sequence sequence = new Sequence(0);
        assertEquals(0, sequence.get());
    }
}
