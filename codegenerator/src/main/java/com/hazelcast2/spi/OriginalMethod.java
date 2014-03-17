package com.hazelcast2.spi;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

public class OriginalMethod extends AbstractMethod {

    public static OriginalMethod build(ExecutableElement methodElement){
        SectorOperation operationAnnotation = methodElement.getAnnotation(SectorOperation.class);
        if(operationAnnotation == null){
            return null;
        }

        return new OriginalMethod(methodElement, operationAnnotation);
    }

    private OriginalMethod(ExecutableElement methodElement, SectorOperation operationAnnotation) {
        returnType = methodElement.getReturnType().toString();
        name = methodElement.getSimpleName().toString();

        for (VariableElement variableElement : methodElement.getParameters()) {
            FormalArgument formalArgument = new FormalArgument();
            if (operationAnnotation.cellbased() && formalArguments.isEmpty()) {
                formalArgument.name = "cell";
            } else {
                formalArgument.name = "arg" + (formalArguments.size() + 1);
            }
            formalArgument.type = variableElement.asType().toString();
            formalArguments.add(formalArgument);
        }
    }
}
