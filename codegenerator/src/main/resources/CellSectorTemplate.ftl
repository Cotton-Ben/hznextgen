package ${class.packageName};

import com.hazelcast2.spi.*;
import com.hazelcast2.util.*;
import com.hazelcast2.nio.*;

import java.util.concurrent.Future;

public final class ${class.name} extends ${class.superName} {

<#list class.methods as method>
    public final static short ${method.functionConstantName} = ${method.functionId};
</#list>

    public ${class.name}(${class.superName}Settings settings) {
        super(settings);
    }
<#list class.methods as method>

    // ===================================================================================================
    //                      ${method.name}
    // ===================================================================================================

    public ${method.returnType} ${method.name}(final long id${method.trailingComma}${method.formalArguments}) {
         final long sequenceAndStatus = claimSlotAndReturnStatus();

         if (sequenceAndStatus == CLAIM_SLOT_REMOTE) {
            InvocationFuture future = remoteInvoke_${method.name}(id${method.trailingComma}${method.actualArguments});
    <#if method.voidReturnType>
            future.getSafely();
            return;
    <#else>
            return (${method.returnTypeAsObject})future.getSafely();
    </#if>
         }

         if (sequenceAndStatus == CLAIM_SLOT_NO_CAPACITY) {
            throw new UnsupportedOperationException();
         }

        if (!isScheduled(sequenceAndStatus)) {
            final long prodSeq = getSequence(sequenceAndStatus);
            //todo: this sucks, we don't want to create new instances.
            final InvocationFuture future = new InvocationFuture();
            final Invocation invocation = getSlot(prodSeq);
            invocation.invocationFuture = future;
            invocation.id = id;
            invocation.functionId = ${method.functionConstantName};
            ${method.mapArgsToInvocation}
            invocation.publish(prodSeq);
    <#if method.voidReturnType>
            future.getSafely();
            return;
    <#else>
            return (${method.returnTypeAsObject})future.getSafely();
    </#if>
        }

        //we didn't actually use the slot since we are going to do a direct call.
        conSeq.inc();
        boolean success = false;
        final ${class.cellName} cell = loadCell(id);
        try{
    <#if method.voidReturnType>
            ${method.targetMethod}(cell ${method.trailingComma}${method.actualArguments});
            success = true;
            process();
    <#else>
            ${method.returnType} result = ${method.targetMethod}(cell ${method.trailingComma}${method.actualArguments});
            success = true;
            process();
            return result;
    </#if>
        }finally{
            if(!success) scheduler.schedule(this);
        }
    }

    public InvocationFuture ${method.asyncName}(final long id${method.trailingComma}${method.formalArguments}) {
        final long sequenceAndStatus = claimSlotAndReturnStatus();

        if (sequenceAndStatus == CLAIM_SLOT_REMOTE) {
            return remoteInvoke_${method.name}(id${method.trailingComma}${method.actualArguments});
        }

        if (sequenceAndStatus == CLAIM_SLOT_NO_CAPACITY) {
            throw new UnsupportedOperationException();
        }

        final boolean schedule = isScheduled(sequenceAndStatus);

        //we need to create a new instance because we are exposing this object to the outside world and can't pool it.
        final InvocationFuture future = new InvocationFuture();

        final long prodSeq = getSequence(sequenceAndStatus);
        final Invocation invocation = getSlot(prodSeq);
        invocation.invocationFuture = future;
        invocation.id = id;
        invocation.functionId = ${method.functionConstantName};
        ${method.mapArgsToInvocation}
        invocation.publish(prodSeq);

        if(schedule) scheduler.schedule(this);
        return future;
    }

    private void deserializeAndInvoke_${method.uniqueMethodName}(final byte[] bytes) throws Exception{
        final long id = IOUtils.readLong(bytes, 8);
        final ${class.cellName} cell = loadCell(id);
        final long callId = IOUtils.readLong(bytes, 16);

    <#if method.hasOneArgOrMore>
        ///todo: if a method only has 'simple' types like string, primitive etc. We should not need to create
        //the ByteArrayObjectDataInput, but we can directly read from the bytes.
        final ByteArrayObjectDataInput in = new ByteArrayObjectDataInput(bytes, 24, serializationService);
    </#if>
    <#if method.voidReturnType>
        ${method.targetMethod}(cell${method.trailingComma} ${method.deserializedInvocationToArgs});
    <#else>
        final ${method.returnType} result = ${method.targetMethod}(cell${method.trailingComma} ${method.deserializedInvocationToArgs});
    </#if>
        //todo: now we need to send back a response to the invoking machine
    }

    private InvocationFuture remoteInvoke_${method.name}(final long id${method.trailingComma}${method.formalArguments}) {
        InvocationFuture invocationFuture = new InvocationFuture();
        try{
            long callId = invocationCompletionService.register(invocationFuture);
            ByteArrayObjectDataOutput out = new ByteArrayObjectDataOutput(serializationService);
            out.writeShort(serviceId);
            out.writeInt(partitionId);
            out.writeShort(${method.functionConstantName});
            out.writeLong(id);
            out.writeLong(callId);
            ${method.argsToSerialize}
            InvocationEndpoint endpoint = endpoints[0];
            endpoint.invoke(out.toByteArray());
        }catch(Exception e){
            invocationFuture.setResponseException(e);
        }
        return invocationFuture;
    }
</#list>

    @Override
    public void process() {
        long consumerSeq = conSeq.get();
        for (; ; ) {
            //todo: we need to checked the locked flag.
            //there is batching that can be done, so instead of doing item by item, you know where the producer is.

            final long prodSeq = getSequence(this.prodSeq.get());
            final long capacity = prodSeq - consumerSeq;
            if (capacity == 0) {
                if (unschedule()) {
                    return;
                }
            } else {
                final Invocation invocation = getSlot(consumerSeq);
                invocation.awaitPublication(consumerSeq);
                if(invocation.bytes == null){
                    invoke(invocation);
                }else{
                    deserializeAndInvoke(invocation);
                }
                invocation.clear();
                consumerSeq++;
                this.conSeq.set(consumerSeq);
            }
        }
    }

    private void invoke(final Invocation invocation) {
        final ${class.cellName} cell = loadCell(invocation.id);
        try{
            switch (invocation.functionId) {
<#list class.methods as method>
                case ${method.functionConstantName}:{
    <#if method.voidReturnType>
                        ${method.targetMethod}(cell ${method.trailingComma}${method.invocationToArgs});
                        invocation.invocationFuture.setVoidResponse();
    <#else>
                        final ${method.returnType} result = ${method.targetMethod}(cell ${method.trailingComma}${method.invocationToArgs});
                        invocation.invocationFuture.setResponse(result);
    </#if>
                    }
                    break;
</#list>
                default:
                    throw new IllegalStateException("Unrecognized function:" + invocation.functionId);
            }
        } catch(Exception e) {
            //todo: this sucks big time because notification is killing for performance
            invocation.invocationFuture.setResponseException(e);
        }
    }

    private void deserializeAndInvoke(final Invocation invocation){
        final byte[] bytes = invocation.bytes;
        final short functionId = IOUtils.readShort(bytes, 6);

        try{
            switch (functionId) {
<#list class.methods as method>
                case ${method.functionConstantName}:
                    deserializeAndInvoke_${method.uniqueMethodName}(bytes);
                    break;
</#list>
                default:
                    throw new IllegalStateException("Unrecognized function:" + functionId);
            }
        } catch (Exception e){
            //todo:
            e.printStackTrace();
        }
    }

    //todo: do we need this method in this subclass or can we move it to sector?
    public void schedule(final InvocationEndpoint source, final byte[] invocationBytes){
        final long sequenceAndStatus = claimSlotAndReturnStatus();

        if (sequenceAndStatus == CLAIM_SLOT_REMOTE) {
            throw new UnsupportedOperationException();
        }

        if (sequenceAndStatus == CLAIM_SLOT_NO_CAPACITY) {
            throw new UnsupportedOperationException();
        }

        final boolean schedule = isScheduled(sequenceAndStatus);

        final long prodSeq = getSequence(sequenceAndStatus);
        final Invocation invocation = getSlot(prodSeq);
        invocation.bytes = invocationBytes;
        invocation.source = source;
        invocation.publish(prodSeq);

        if(schedule) scheduler.schedule(this);
    }
}

