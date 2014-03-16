package com.hazelcast2.core;

public interface IFunction<E,R> {
    R apply(E item);
}
