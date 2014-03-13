package com.hazelcast2.nio.impl;

import com.hazelcast2.nio.Connection;
import com.hazelcast2.nio.ConnectionManager;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ConnectionManagerImpl implements ConnectionManager {

    public final ConcurrentMap<String, Connection> connections = new ConcurrentHashMap<>();

    @Override
    public Connection getConnection(String address) {
        return connections.get(address);
    }
}
