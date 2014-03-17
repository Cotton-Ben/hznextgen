package ${class.packageName};

import com.hazelcast2.spi.*;
import com.hazelcast2.internal.util.*;
import com.hazelcast2.internal.nio.*;

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

    <@renderBaseMethod method/>

    <#if method.asyncMethod??>
    <@renderAsyncMethod method method.asyncMethod/>
    </#if>

    <@renderDeserializeAndInvoke method/>

    <@renderRemoteInvokeMethod method/>
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
    </#if>
    <#if method.voidReturnType>
                        ${method.targetMethod}(${method.invocationToArgs});
                        invocation.invocationFuture.setVoidResponse();
     <#else>
                        final ${method.returnType} result = ${method.targetMethod}(${method.invocationToArgs});
                        invocation.invocationFuture.setResponse(result);
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

<#-- ========================================================================== -->
<#-- macro renderBaseMethod -->
<#-- ========================================================================== -->

<#macro renderBaseMethod method>
    public ${method.returnType} ${method.name}(${method.formalArguments}) {
        final long sequenceAndStatus = claimSlotAndReturnStatus();

        if (sequenceAndStatus == CLAIM_SLOT_REMOTE) {
            InvocationFuture future = remoteInvoke_${method.name}(${method.actualArguments});
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
            ${method.originalMethod.name}(${method.originalMethod.actualArguments});
            success = true;
            process();
    <#else>
            ${method.returnType} result = ${method.originalMethod.name}(${method.originalMethod.actualArguments});
            success = true;
            process();
            return result;
    </#if>
        }finally{
            if(!success) scheduler.schedule(this);
        }
    }
</#macro>

<#-- ========================================================================== -->
<#-- macro renderRemoteInvokeMethod -->
<#-- ========================================================================== -->

<#macro renderRemoteInvokeMethod method>
    private InvocationFuture remoteInvoke_${method.name}(${method.formalArguments}) {
        InvocationFuture invocationFuture = new InvocationFuture();
        try{
            final long callId = invocationCompletionService.register(invocationFuture);
            final ByteArrayObjectDataOutput out = new ByteArrayObjectDataOutput(serializationService);
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
            ${method.serializeArgs}
            InvocationEndpoint primary = replicaSet[0];
            primary.send(out.toByteArray());
        }catch(Exception e){
            invocationFuture.setResponseException(e);
        }
        return invocationFuture;
    }
</#macro>

<#-- ========================================================================== -->
<#-- macro renderAsyncMethod -->
<#-- ========================================================================== -->

<#macro renderAsyncMethod method asyncMethod>
    public ${asyncMethod.returnType} ${asyncMethod.name}(${asyncMethod.formalArguments}) {
        final long sequenceAndStatus = claimSlotAndReturnStatus();

        if (sequenceAndStatus == CLAIM_SLOT_REMOTE) {
            return remoteInvoke_${method.name}(${method.actualArguments});
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
        invocation.functionId = ${method.functionConstantName};
        ${method.mapArgsToInvocation}
        invocation.publish(prodSeq);

        if(schedule) scheduler.schedule(this);
        return future;
    }
</#macro>

<#-- ========================================================================== -->
<#-- macro renderDeserializeAndInvokeMethod -->
<#-- ========================================================================== -->

<#macro renderDeserializeAndInvoke method>
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
        ${method.targetMethod}(${method.deserializedInvocationToArgs});
    <#else>
        final ${method.returnType} result = ${method.targetMethod}(${method.deserializedInvocationToArgs});
    </#if>

        final InvocationEndpoint source = invocation.source;
        if(source == null) return;

    <#switch method.returnType>
        <#case "void">
        final byte[] buffer = new byte[10];
        IOUtils.writeShort(invocationCompletionService.getServiceId(), buffer, 0);
        IOUtils.writeLong(callId, buffer, 2);
        source.send(buffer);
            <#break>
        <#case "boolean">
        final byte[] buffer = new byte[11];
        IOUtils.writeShort(invocationCompletionService.getServiceId(), buffer, 0);
        IOUtils.writeLong(callId, buffer, 2);
        IOUtils.writeBoolean(result, buffer, 10);
        source.send(buffer);
            <#break>
        <#case "byte">
        final byte[] buffer = new byte[11];
        IOUtils.writeShort(invocationCompletionService.getServiceId(), buffer, 0);
        IOUtils.writeLong(callId, buffer, 2);
        IOUtils.writeByte(result, buffer, 10);
        source.send(buffer);
            <#break>
        <#case "char">
        final byte[] buffer = new byte[12];
        IOUtils.writeShort(invocationCompletionService.getServiceId(), buffer, 0);
        IOUtils.writeLong(callId, buffer, 2);
        IOUtils.writeChar(result, buffer, 10);
        source.send(buffer);
            <#break>
        <#case "short">
        final byte[] buffer = new byte[12];
        IOUtils.writeShort(invocationCompletionService.getServiceId(), buffer, 0);
        IOUtils.writeLong(callId, buffer, 2);
        IOUtils.writeShort(result, buffer, 10);
        source.send(buffer);
            <#break>
        <#case "int">
        final byte[] buffer = new byte[14];
        IOUtils.writeShort(invocationCompletionService.getServiceId(), buffer, 0);
        IOUtils.writeLong(callId, buffer, 2);
        IOUtils.writeInt(result, buffer, 10);
        source.send(buffer);
            <#break>
        <#case "float">
        final byte[] buffer = new byte[14];
        IOUtils.writeShort(invocationCompletionService.getServiceId(), buffer, 0);
        IOUtils.writeLong(callId, buffer, 2);
        IOUtils.writeFloat(result, buffer, 10);
        source.send(buffer);
            <#break>
        <#case "long">
        final byte[] buffer = new byte[18];
        IOUtils.writeShort(invocationCompletionService.getServiceId(), buffer, 0);
        IOUtils.writeLong(callId, buffer, 2);
        IOUtils.writeLong(result, buffer, 10);
        source.send(buffer);
            <#break>
        <#case "double">
        final byte[] buffer = new byte[18];
        IOUtils.writeShort(invocationCompletionService.getServiceId(), buffer, 0);
        IOUtils.writeLong(callId, buffer, 2);
        IOUtils.writeLong(result, buffer, 10);
        source.send(buffer);
            <#break>
        <#default>
        final ByteArrayObjectDataOutput out = new ByteArrayObjectDataOutput(serializationService);
        out.writeShort(invocationCompletionService.getServiceId());
        out.writeLong(callId);
        out.writeObject(result);
        source.send(out.toByteArray());
    </#switch>
    }
</#macro>