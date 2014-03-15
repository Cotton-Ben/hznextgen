package com.hazelcast2.serialization;

import com.hazelcast2.internal.nio.ByteArrayObjectDataInput;
import com.hazelcast2.internal.nio.ByteArrayObjectDataOutput;

import java.io.*;

/**
 * Very shitty implementation and can only deal with java serialization.
 */
public class SerializationService {

    public <E> E readObject(ByteArrayObjectDataInput input) {
        try {

            ByteArrayInputStream i = new ByteArrayInputStream(input.getBuffer());
            ObjectInputStream in = new ObjectInputStream(i);
            Object result = in.readObject();
            if (result instanceof Null) {
                return null;
            } else {
                return (E) result;
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new HazelcastSerializationException(e);
        }
    }

    public void writeObject(ByteArrayObjectDataOutput output, Object object) {
        if (object == null) {
            object = new Null();
        }

        try {
            if (object instanceof Serializable) {
                ByteArrayOutputStream o = new ByteArrayOutputStream();
                ObjectOutputStream out = new ObjectOutputStream(o);
                out.writeObject(object);
                out.close();
                byte[] bytes = o.toByteArray();
                output.write(bytes, 0, bytes.length);
                return;
            }

            throw new UnsupportedOperationException();
        } catch (IOException e) {
            throw new HazelcastSerializationException(e);
        }
    }

    private static class Null implements Serializable {

    }
}
