package com.hazelcast2.nio;

import com.hazelcast2.spi.InvocationEndpoint;

/**
 * The Gateway is the entrance to the IO System. So when a 'packet' of data is received, is can be send to the
 * gateway where it is dispatched to the appropriate service.
 */
public interface Gateway {

    /**
     *
     * @param source
     * @param bytes
     */
    void dispatch(InvocationEndpoint source, byte[] bytes);
}
