package com.hazelcast2.nio;

/**
 * The Gateway is the entrance to the IO System. So when a 'packet' of data is received, is can be send to the
 * gateway where it is dispatched to the appropriate service.
 */
public interface Gateway {

    //todo: the sender so we can do a callback.
    void dispatch(byte[] bytes);
}
