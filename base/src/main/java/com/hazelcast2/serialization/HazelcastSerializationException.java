package com.hazelcast2.serialization;

import com.hazelcast2.core.HazelcastException;

/**
 * This is an exception thrown when an exception occurs while serializing/deserializing objects.
 */
public class HazelcastSerializationException extends HazelcastException {

    public HazelcastSerializationException(final String message) {
        super(message);
    }

    public HazelcastSerializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public HazelcastSerializationException(Throwable e) {
        super(e);
    }
}
