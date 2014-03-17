package com.hazelcast2.spi;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

public class OriginalMethod extends AbstractMethod {

    public static class Builder {
        public ExecutableElement methodElement;

        public OriginalMethod build() {
            SectorOperation operationAnnotation = methodElement.getAnnotation(SectorOperation.class);
            if (operationAnnotation == null) {
                return null;
            }

            OriginalMethod method = new OriginalMethod();
            method.returnType = methodElement.getReturnType().toString();
            method.name = methodElement.getSimpleName().toString();

            for (VariableElement variableElement : methodElement.getParameters()) {
                FormalArgument formalArgument = new FormalArgument();
                if (operationAnnotation.cellbased() && method.formalArguments.isEmpty()) {
                    formalArgument.name = "cell";
                } else {
                    formalArgument.name = "arg" + (method.formalArguments.size() + 1);
                }
                formalArgument.type = variableElement.asType().toString();
                method.formalArguments.add(formalArgument);
            }

            return method;
        }
    }

    private OriginalMethod() {
    }
}
