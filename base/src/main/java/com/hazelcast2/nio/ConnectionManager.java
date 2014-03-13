package com.hazelcast2.nio;

public interface ConnectionManager {

    Connection getConnection(String address);
}
