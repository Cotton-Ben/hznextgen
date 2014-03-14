package com.hazelcast2.spi;

public interface SpiService {

    /**
     * Returns the id that uniquely identifies this SpiService.
     *
     * @return
     */
    short getServiceId();

    /**
     * Remote invocation enters here.
     *
     * @param invocationBytes
     */
    void dispatch(byte[] invocationBytes);

}
