package com.hazelcast2.core;

public interface DistributedObject {

    String getName();

    long getId();

    /**
     * Destroys the object.
     *
     * Can safely be called when already destroyed.
     *
     * Unlike hazelcast 3, it really destroys the object. If another proxy is referring to this object,
     * it will get an error because the object with that given id not found anymore.
     */
    void destroy();

    boolean isDestroyed();

}
