package com.hazelcast2.nio;

import java.nio.ByteBuffer;

/**
 * Is responsible for sending an operation to a certain machine.
 *
 * We don't have an actual Operation object unlike Hazelcast 3. Only bytes will be flowing through the system.
 */
public interface Connection {

    /**
     * Gets a ByteBuffer that can be used to post the content of an operation on.
     *
     * The actual implementation is probably going to
     *
     * @return the created ByteBuffer.
     */
    ByteBuffer newByteBuffer();

    Address getAddress();

    //todo: address

    /**
     * Sends the operation.
     *
     * After this method is called, the ByteBuffer should not be used. This makes it possible that the Connection
     * can pool ByteBuffers instead of needing to create them.
     *
     * @param buffer the ByteBuffer containing the operation.
     */
    void send(ByteBuffer buffer);
}
