package com.hazelcast2.spi;

public interface SpiService {

    /**
     * Returns the id that uniquely identifies this SpiService.
     *
     * In Hazelcast 3 everything is done with {@link #getServiceName()}. The problem is that
     * for every remote invocation, the servicename needs to be send over the wire. Using a short
     * is a lot cheaper.
     *
     * @return
     */
    short getServiceId();

    /**
     * Returns the name that uniquely identifies this SpiService.
     * @return
     */
    String getServiceName();

    /**
     * Remote invocation enters here.
     *
     * @param invocationBytes
     */
    void dispatch(InvocationEndpoint source, byte[] invocationBytes);

}
