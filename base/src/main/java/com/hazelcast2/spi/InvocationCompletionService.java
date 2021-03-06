package com.hazelcast2.spi;

import com.hazelcast2.internal.nio.IOUtils;
import com.hazelcast2.internal.util.InvocationFuture;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The InvocationCompletionService is responsible for notifying an 'invocation' that the invocation is complete.
 * So when a call is send to a remote system, a completion token (call id) is generated and the invoker will
 * register a future. When the remote system returns a response, based on the call id, this service will look
 * up the future and notify it.
 * <p/>
 * In the Hazelcast 3 architecture this functionality is embedded within the OperationService.
 * <p/>
 * This service should not be responsible for deserialization. It would be best if we can callback to a piece
 * of generated code that knows how to read out the response.
 * <p/>
 * todo:
 * - dealing with calls that don't complete
 * - dealing with calls that are slow to complete
 * <p/>
 * Each call should also have a time-last-checked (or a ttl which is updater periodically). If a call doesn't
 * get a response from a remote system or
 * <p/>
 * todo:
 * - can we use a ringbuffer structure here as well? The problem is with calls not completing in the same
 * order.
 * <p/>
 * todo:
 * - could this completion service be used as the threading mechanism for async call completion? If there is a
 * pool of threads available from this structure, then we can immediately offload the notifications
 * <p/>
 * <p/>
 * Message content
 * 2 bytes: service id
 * 8 bytes: call id
 * remaining bytes: response
 */
//todo: very inefficient implementation.
public class InvocationCompletionService implements SpiService {

    public static final String SERVICE_NAME = "hz:impl:invocationCompletionService";

    private final short serviceId;
    private final ConcurrentMap<Long, InvocationFuture> calls = new ConcurrentHashMap<>();
    private final AtomicLong callIdGenerator = new AtomicLong();

    public InvocationCompletionService(short serviceId) {
        this.serviceId = serviceId;
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    public short getServiceId() {
        return serviceId;
    }

    @Override
    public void dispatch(InvocationEndpoint source, byte[] invocationBytes) {
        final long callId = IOUtils.readLong(invocationBytes, 2);
        final InvocationFuture f = calls.remove(callId);
        if (f == null) {
            System.out.println("No invocation found for callid:" + callId);
            return;
        }

        //todo: this part is no good because the invocation future should not
        if (invocationBytes.length == 10) {
            f.setVoidResponse();
        } else {
            f.setResponse(IOUtils.readLong(invocationBytes, 10));
        }
    }

    public long register(InvocationFuture future) {
        long callId = callIdGenerator.incrementAndGet();
        calls.put(callId, future);
        return callId;
    }

}
