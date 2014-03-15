package ${class.packageName};

import com.hazelcast2.spi.*;
import com.hazelcast2.internal.util.*;

import java.util.concurrent.Future;

public final class ${class.name} extends ${class.superName} {

<#list class.methods as method>
    private final static short ${method.functionConstantName} = ${method.functionId};
</#list>

    public ${class.name}(${class.superName}Settings settings) {
        super(settings);
    }

    private long doClaimSlotAndReturnStatus(){
        final long sequenceAndStatus = claimSlotAndReturnStatus();

        if (sequenceAndStatus == CLAIM_SLOT_REMOTE) {
            throw new UnsupportedOperationException();
        }

        if (sequenceAndStatus == CLAIM_SLOT_NO_CAPACITY) {
            throw new UnsupportedOperationException();
        }

        return sequenceAndStatus;
    }

<#list class.methods as method>

     public Future<${method.returnTypeAsObject}> ${method.asyncName}(${method.formalArguments}) {
        final long sequenceAndStatus = doClaimSlotAndReturnStatus();
        final boolean schedule = isScheduled(sequenceAndStatus);

        //we need to create a new instance because we are exposing this object to the outside world and can't
        //pool it.
        final InvocationFuture future = new InvocationFuture();

        final long prodSeq = getSequence(sequenceAndStatus);
        final InvocationSlot invocation = getSlot(prodSeq);
        invocation.invocationFuture = future;
        //invocation.id = id;
        invocation.functionId = ${method.functionConstantName};
        ${method.mapArgsToInvocation}
        invocation.publish(prodSeq);

        if(schedule) scheduler.schedule(this);
        return future;
    }

    public ${method.returnType} ${method.name}(${method.formalArguments}) {
        final long sequenceAndStatus = doClaimSlotAndReturnStatus();

        if (!isScheduled(sequenceAndStatus)) {
            final long prodSeq = getSequence(sequenceAndStatus);
            //todo: this sucks, we don't want to create new instances.
            final InvocationFuture future = new InvocationFuture();
            final InvocationSlot invocation = getSlot(prodSeq);
            invocation.invocationFuture = future;
            //invocation.id = id;
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
        try{
    <#if method.voidReturnType>
            ${method.targetMethod}(${method.actualArguments});
            success = true;
            process();
    <#else>
            ${method.returnType} result = ${method.targetMethod}(${method.actualArguments});
            success = true;
            process();
            return result;
    </#if>
        }finally{
            if(!success) scheduler.schedule(this);
        }
    }
</#list>

    public void process() {
        long consumerSeq = this.conSeq.get();
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
                final InvocationSlot invocation = getSlot(consumerSeq);
                invocation.awaitPublication(consumerSeq);
                dispatch(invocation);
                invocation.clear();
                consumerSeq++;
                this.conSeq.set(consumerSeq);
            }
        }
    }

    public void dispatch(InvocationSlot invocation) {
        try{
            switch (invocation.functionId) {
<#list class.methods as method>
                case ${method.functionConstantName}:
                    {
    <#if method.voidReturnType>
                        ${method.targetMethod}(${method.invocationToArgs});
                        invocation.invocationFuture.setVoidResponse();
    <#else>
                        ${method.returnType} result = ${method.targetMethod}(${method.invocationToArgs});
                        invocation.invocationFuture.setResponse(result);
    </#if>
                    }
                    break;
</#list>
                default:
                    throw new IllegalStateException("Unrecognized function:" + invocation.functionId);
            }
        }catch(Exception e){
            //todo: this sucks big time because notification is killing for performance
            invocation.invocationFuture.setResponseException(e);
        }
    }

    public void schedule(InvocationEndpoint source, byte[] invocationBytes){
    }
}

