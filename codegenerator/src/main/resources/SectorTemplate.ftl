package ${class.packageName};

import com.hazelcast2.spi.*;
import com.hazelcast2.internal.util.*;
import com.hazelcast2.internal.nio.*;

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

    <#if method.cellbased>
    public ${method.returnType} ${method.name}(final long id${method.trailingComma}${method.formalArguments}) {
    <#else>
    public ${method.returnType} ${method.name}(${method.formalArguments}) {
    </#if>
        final long sequenceAndStatus = claimSlotAndReturnStatus();

        if (sequenceAndStatus == CLAIM_SLOT_REMOTE) {
            <#if method.cellbased>
            InvocationFuture future = remoteInvoke_${method.name}(id${method.trailingComma}${method.actualArguments});
            <#else>
            InvocationFuture future = remoteInvoke_${method.name}(${method.actualArguments});
            </#if>
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
            final InvocationFuture future = new InvocationFuture(serializationService);
            final InvocationSlot invocation = getSlot(prodSeq);
            invocation.invocationFuture = future;
    <#if method.cellbased>
            invocation.id = id;
    </#if>
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
    <#if method.cellbased>
        final ${class.cellName} cell = loadCell(id);
    </#if>
        try{
    <#if method.voidReturnType>
        <#if method.cellbased>
            ${method.targetMethod}(cell ${method.trailingComma}${method.actualArguments});
        <#else>
            ${method.targetMethod}(${method.actualArguments});
        </#if>
            success = true;
            process();
    <#else>
            <#if method.cellbased>
            ${method.returnType} result = ${method.targetMethod}(cell ${method.trailingComma}${method.actualArguments});
            <#else>
            ${method.returnType} result = ${method.targetMethod}(${method.actualArguments});
            </#if>
            success = true;
            process();
            return result;
    </#if>
        }finally{
            if(!success) scheduler.schedule(this);
        }
    }

    <#if method.cellbased>
    public InvocationFuture ${method.asyncName}(final long id${method.trailingComma}${method.formalArguments}) {
    <#else>
    public InvocationFuture ${method.asyncName}(${method.formalArguments}) {
    </#if>
        final long sequenceAndStatus = claimSlotAndReturnStatus();

        if (sequenceAndStatus == CLAIM_SLOT_REMOTE) {
            <#if method.cellbased>
            return remoteInvoke_${method.name}(id${method.trailingComma}${method.actualArguments});
            <#else>
            return remoteInvoke_${method.name}(${method.actualArguments});
            </#if>
        }

        if (sequenceAndStatus == CLAIM_SLOT_NO_CAPACITY) {
            throw new UnsupportedOperationException();
        }

        final boolean schedule = isScheduled(sequenceAndStatus);

        //we need to create a new instance because we are exposing this object to the outside world and can't pool it.
        final InvocationFuture future = new InvocationFuture(serializationService);

        final long prodSeq = getSequence(sequenceAndStatus);
        final InvocationSlot invocation = getSlot(prodSeq);
        invocation.invocationFuture = future;
    <#if method.cellbased>
        invocation.id = id;
    </#if>
        invocation.functionId = ${method.functionConstantName};
        ${method.mapArgsToInvocation}
        invocation.publish(prodSeq);

        if(schedule) scheduler.schedule(this);
        return future;
    }

    private void deserializeAndInvoke_${method.uniqueMethodName}(final InvocationSlot invocation) throws Exception{
        final byte[] bytes = invocation.bytes;
    <#if method.cellbased>
        final long id = IOUtils.readLong(bytes, 8);
        final ${class.cellName} cell = loadCell(id);
    </#if>
        final long callId = IOUtils.readLong(bytes, 16);

    <#if method.hasOneArgOrMore>
        final ByteArrayObjectDataInput in = new ByteArrayObjectDataInput(bytes, 24, serializationService);
    </#if>
    <#if method.voidReturnType>
        <#if method.cellbased>
        ${method.targetMethod}(cell${method.trailingComma} ${method.deserializedInvocationToArgs});
        <#else>
        ${method.targetMethod}(${method.deserializedInvocationToArgs});
        </#if>
    <#else>
        <#if method.cellbased>
        final ${method.returnType} result = ${method.targetMethod}(cell${method.trailingComma} ${method.deserializedInvocationToArgs});
        <#else>
        final ${method.returnType} result = ${method.targetMethod}(${method.deserializedInvocationToArgs});
        </#if>
    </#if>

        final InvocationEndpoint source = invocation.source;
        if(source != null){
            final ByteArrayObjectDataOutput out = new ByteArrayObjectDataOutput(serializationService);
            out.writeShort(invocationCompletionService.getServiceId());
            out.writeLong(callId);
    <#switch method.returnType>
        <#case "void">
            <#break>
        <#case "long">
            out.writeLong(result);
            <#break>
        <#case "boolean">
            out.writeBoolean(result);
            <#break>
        <#case "int">
            out.writeInt(result);
            <#break>
        <#case "byte">
            out.writeByte(result);
            <#break>
        <#case "float">
            out.writeFloat(result);
            <#break>
        <#case "double">
            out.writeDouble(result);
            <#break>
        <#case "char">
            out.writeChar(result);
            <#break>
        <#case "short">
            out.writeShort(result);
            <#break>
        <#default>
            out.writeObject(result);
    </#switch>
            source.send(out.toByteArray());
        }
    }

    <#if method.cellbased>
    private InvocationFuture remoteInvoke_${method.name}(final long id${method.trailingComma}${method.formalArguments}) {
    <#else>
    private InvocationFuture remoteInvoke_${method.name}(${method.formalArguments}) {
    </#if>
        InvocationFuture invocationFuture = new InvocationFuture();
        try{
            final long callId = invocationCompletionService.register(invocationFuture);
            ByteArrayObjectDataOutput out = new ByteArrayObjectDataOutput(serializationService);
            out.writeShort(serviceId);
            out.writeInt(partitionId);
            out.writeShort(${method.functionConstantName});
   <#if method.cellbased>
            out.writeLong(id);
   <#else>
            //todo: temporary hack to make sure we can deserialize
            out.writeLong(0);
   </#if>
            out.writeLong(callId);
            ${method.argsToSerialize}
            InvocationEndpoint endpoint = endpoints[0];
            endpoint.send(out.toByteArray());
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
            final long prodSeq = getSequence(this.prodSeq.get());
            final long capacity = prodSeq - consumerSeq;
            if (capacity == 0) {
                if (unschedule()) {
                    return;
                }
            } else {
                final InvocationSlot invocation = getSlot(consumerSeq);
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

    private void invoke(final InvocationSlot invocation) {

        try{
            switch (invocation.functionId) {
<#list class.methods as method>
                case ${method.functionConstantName}:{
    <#if method.cellbased>
                        final ${class.cellName} cell = loadCell(invocation.id);
        <#if method.voidReturnType>
                        ${method.targetMethod}(cell ${method.trailingComma}${method.invocationToArgs});
                        invocation.invocationFuture.setVoidResponse();
        <#else>
                        final ${method.returnType} result = ${method.targetMethod}(cell ${method.trailingComma}${method.invocationToArgs});
                        invocation.invocationFuture.setResponse(result);
        </#if>
    <#else>

        <#if method.voidReturnType>
                        ${method.targetMethod}(${method.invocationToArgs});
                        invocation.invocationFuture.setVoidResponse();
        <#else>
                        final ${method.returnType} result = ${method.targetMethod}(${method.invocationToArgs});
                        invocation.invocationFuture.setResponse(result);
        </#if>
    </#if>
                    }
                    break;
</#list>
                default:
                    throw new IllegalStateException("Unrecognized function:" + invocation.functionId);
            }
        } catch(Exception e) {
            invocation.invocationFuture.setResponseException(e);
        }
    }

    private void deserializeAndInvoke(final InvocationSlot invocation){
        final short functionId = IOUtils.readShort(invocation.bytes, 6);

        try{
            switch (functionId) {
<#list class.methods as method>
                case ${method.functionConstantName}:
                    deserializeAndInvoke_${method.uniqueMethodName}(invocation);
                    break;
</#list>
                default:
                    throw new IllegalStateException("Unrecognized function:" + functionId);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}

