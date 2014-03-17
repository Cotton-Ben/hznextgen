package com.hazelcast2.spi;

import javax.lang.model.element.ExecutableElement;

public class AsyncMethod extends AbstractMethod {

    public AsyncMethod(ExecutableElement methodElement, SectorMethod sectorMethod) {
        String methodName = methodElement.getSimpleName().toString();

        this.name = "hz_async" + CodeGenerationUtils.capitalizeFirstLetter(methodName);
        this.returnType = "InvocationFuture";
        this.formalArguments = sectorMethod.formalArguments;
    }
}
