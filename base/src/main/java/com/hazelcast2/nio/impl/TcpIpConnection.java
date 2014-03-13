package com.hazelcast2.nio.impl;

import com.hazelcast2.nio.Address;
import com.hazelcast2.nio.Connection;

import java.nio.ByteBuffer;

public class TcpIpConnection implements Connection {

    @Override
    public ByteBuffer newByteBuffer() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Address getAddress() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void send(ByteBuffer buffer) {
        throw new UnsupportedOperationException();
    }
}
