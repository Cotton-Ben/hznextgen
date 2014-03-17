package com.hazelcast2.spi;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

public class AsyncMethod extends AbstractMethod {

    public static class Builder {
        public ExecutableElement methodElement;
        public SectorMethod sectorMethod;
        public TypeElement classElement;

        AsyncMethod build() {
            AsyncMethod method = new AsyncMethod();

            String methodName = methodElement.getSimpleName().toString();

            method.name = "hz_async" + CodeGenerationUtils.capitalizeFirstLetter(methodName);
            method.returnType = "InvocationFuture";
            method.formalArguments = sectorMethod.formalArguments;

            if (!find(method)) {
                return null;
            }

            return method;
        }

        public boolean find(AsyncMethod asyncMethod) {
            for (Element enclosedElement : classElement.getEnclosedElements()) {
                if (!enclosedElement.getKind().equals(ElementKind.METHOD)) {
                    continue;
                }
                ExecutableElement methodElement = (ExecutableElement) enclosedElement;
                if (!methodElement.getSimpleName().toString().equals(asyncMethod.name)) {
                    continue;
                }

                if (methodElement.getParameters().size() != asyncMethod.formalArguments.size()) {
                    continue;
                }

                return true;
            }

            return false;
        }
    }

    private AsyncMethod() {

    }
}
