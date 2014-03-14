package com.hazelcast2.core;

public class HazelcastException extends RuntimeException {

    public HazelcastException() {
    }

    public HazelcastException(final String message) {
        super(message);
    }

    public HazelcastException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public HazelcastException(final Throwable cause) {
        super(cause);
    }
}
