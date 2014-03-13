package ${class.packageName};

import com.hazelcast2.spi.Invocation;
import com.hazelcast2.util.InvocationFuture;
import com.hazelcast2.util.IOUtils;
import com.hazelcast2.util.ByteArrayObjectDataInput;

import java.util.concurrent.Future;

public final class ${class.name} extends ${class.superName} {

<#list class.methods as method>
    public final static short ${method.functionConstantName} = ${method.functionId};
</#list>

    public ${class.name}(${class.superName}Settings settings) {
        super(settings);
    }

    //todo: this method is replicated in alll sectors..
    private long doClaimSlotAndReturnStatus(){
        final long sequenceAndStatus = claimSlotAndReturnStatus();

        if (sequenceAndStatus == CLAIM_SLOT_LOCKED) {
            throw new UnsupportedOperationException();
        }

        if (sequenceAndStatus == CLAIM_SLOT_NO_CAPACITY) {
            throw new UnsupportedOperationException();
        }

        return sequenceAndStatus;
    }
<#list class.methods as method>

     public Future<${method.returnTypeAsObject}> ${method.asyncName}(final long id${method.trailingComma}${method.formalArguments}) {
        final long sequenceAndStatus = doClaimSlotAndReturnStatus();
        final boolean schedule = isScheduled(sequenceAndStatus);

        //we need to create a new instance because we are exposing this object to the outside world and can't
        //pool it.
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

    public ${method.returnType} ${method.name}(final long id${method.trailingComma}${method.formalArguments}) {
        final long sequenceAndStatus = doClaimSlotAndReturnStatus();

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
            //instead of waiting, is it possible to help out other partitions/subsystems?
            //the thing you need to be careful with is that you should not keep building up
            //stackframes, because eventually you will get a stackoverflow.
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
                case ${method.functionConstantName}:
                    {
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
<#list class.methods as method>

    private void deserializeAndInvoke_${method.uniqueMethodName}(final byte[] bytes) throws Exception{
        final long id = IOUtils.readLong(bytes, 8);
        final ${class.cellName} cell = loadCell(id);

    <#if method.hasOneArgOrMore>
        ///todo: if a method only has 'simple' types like string, primitive etc. We should not need to create
        //the ByteArrayObjectDataInput, but we can directly read from the bytes.
        final ByteArrayObjectDataInput in = new ByteArrayObjectDataInput(bytes, 16, serializationService);
    </#if>
    <#if method.voidReturnType>
        ${method.targetMethod}(cell${method.trailingComma} ${method.deserializedInvocationToArgs});
    <#else>
        final ${method.returnType} result = ${method.targetMethod}(cell${method.trailingComma} ${method.deserializedInvocationToArgs});
    </#if>
        //todo: now we need to send back a response to the invoking machine
    }
</#list>

    //todo: do we need this method in this subclass or can we move it to sector?
    public void schedule(final byte[] invocationBytes){
        final long sequenceAndStatus = doClaimSlotAndReturnStatus();

        final boolean schedule = isScheduled(sequenceAndStatus);

        final long prodSeq = getSequence(sequenceAndStatus);
        final Invocation invocation = getSlot(prodSeq);
        invocation.bytes = invocationBytes;
        invocation.publish(prodSeq);

        if(schedule) scheduler.schedule(this);
    }
}

