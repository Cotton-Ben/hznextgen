package ${class.packageName};

import com.hazelcast2.spi.Invocation;
import com.hazelcast2.spi.PartitionSettings;
import com.hazelcast2.util.InvocationFuture;

import java.util.concurrent.Future;

public final class ${class.name} extends ${class.superName} {

<#list class.methods as method>
    private final static int ${method.functionConstantName} = ${method_index};
</#list>

    public ${class.name}(PartitionSettings partitionSettings) {
        super(partitionSettings);
    }

    private long doClaimSlot(){
        final long x = claimSlot();

        if (x == CLAIM_SLOT_LOCKED) {
            throw new UnsupportedOperationException();
        }

        if (x == CLAIM_SLOT_NO_CAPACITY) {
            throw new UnsupportedOperationException();
        }

        return x;
    }

<#list class.methods as method>

     public Future<${method.returnTypeAsObject}> ${method.asyncName}(final long id${method.trailingComma}${method.formalArguments}) {
        final long x = doClaimSlot();
        final boolean schedule = isScheduled(x);

        //we need to create a new instance because we are exposing this object to the outside world and can't
        //pool it.
        final InvocationFuture future = new InvocationFuture();

        final long prodSeq = x >> 2;
        final Invocation invocation = getSlot(prodSeq);
        invocation.invocationFuture = future;
        invocation.id = id;
        invocation.functionId = ${method.functionConstantName};
        ${method.mapArgsToInvocation}
        invocation.commit(prodSeq);

        if(schedule) scheduler.schedule(this);
        return future;
    }

    public ${method.returnType} ${method.name}(final long id${method.trailingComma}${method.formalArguments}) {
        final long x = doClaimSlot();

        if (!isScheduled(x)) {
            final long prodSeq = x >> 2;
            //todo: this sucks, we don't want to create new instances.
            final InvocationFuture future = new InvocationFuture();
            final Invocation invocation = getSlot(prodSeq);
            invocation.invocationFuture = future;
            invocation.id = id;
            invocation.functionId = ${method.functionConstantName};
            ${method.mapArgsToInvocation}
            invocation.commit(prodSeq);
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
        conSeq++;
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

    public void process() {
        long consumerSeq = this.conSeq;
        for (; ; ) {
            //todo: we need to checked the locked flag.
            //there is batching that can be done, so instead of doing item by item, you know where the producer is.

            final long prodSeq = this.prodSeq >> 2;
            final long capacity = prodSeq - consumerSeq;
            if (capacity == 0) {
                if (unschedule()) {
                    return;
                }
            } else {
                final Invocation invocation = getSlot(consumerSeq);
                invocation.waitCommit(consumerSeq);
                dispatch(invocation);
                invocation.clear();
                consumerSeq++;
                this.conSeq = consumerSeq;
            }
        }
    }

    public void dispatch(Invocation invocation) {

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
                        ${method.returnType} result = ${method.targetMethod}(cell ${method.trailingComma}${method.invocationToArgs});
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
}