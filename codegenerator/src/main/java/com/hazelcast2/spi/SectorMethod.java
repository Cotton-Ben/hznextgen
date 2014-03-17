package com.hazelcast2.spi;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.LinkedList;
import java.util.List;

public class SectorMethod extends AbstractMethod {

    public static class Builder {
        public ExecutableElement methodElement;
        public SectorClassModel sectorClassModel;
        public TypeElement classElement;

        public SectorMethod build() {
            SectorMethod method = new SectorMethod();
            String methodName = methodElement.getSimpleName().toString();
            SectorOperation operationAnnotation = methodElement.getAnnotation(SectorOperation.class);
            if(operationAnnotation==null){
                return null;
            }

            int argCount = methodElement.getParameters().size();

            method.cellbased = operationAnnotation.cellbased();
            method.name = "hz_" + methodName;

            method.returnType = methodElement.getReturnType().toString();
            method.invocationClassName = CodeGenerationUtils.capitalizeFirstLetter(methodName) + argCount + "Invocation";
            method.targetMethod = methodName;
            method.readonly = operationAnnotation.readonly();
            method.functionId = sectorClassModel.getMethods().size();

            List<FormalArgument> args = new LinkedList<>();

            for (VariableElement variableElement : methodElement.getParameters()) {
                FormalArgument formalArgument = new FormalArgument();
                if (method.cellbased && args.isEmpty()) {
                    formalArgument.name = "id";
                    formalArgument.type = "long";
                } else {
                    formalArgument.name = "arg" + (args.size() + 1);
                    formalArgument.type = variableElement.asType().toString();
                }
                args.add(formalArgument);
            }

            method.formalArguments = args;

            buildOriginalMethos(method);

            buildAsyncMethod(method);

            return method;
        }

        private void buildAsyncMethod(SectorMethod sectorMethod) {
            AsyncMethod.Builder asyncBuilder = new AsyncMethod.Builder();
            asyncBuilder.methodElement = methodElement;
            asyncBuilder.sectorMethod = sectorMethod;
            asyncBuilder.classElement = classElement;
            sectorMethod.asyncMethod = asyncBuilder.build();
        }

        private void buildOriginalMethos(SectorMethod sectorMethod) {
            OriginalMethod.Builder originalBuilder = new OriginalMethod.Builder();
            originalBuilder.methodElement = methodElement;
            sectorMethod.originalMethod = originalBuilder.build();
        }
    }

    public AsyncMethod asyncMethod;
    public String targetMethod;
    public String invocationClassName;
    public boolean readonly = false;
    public boolean cellbased = false;
    public OriginalMethod originalMethod;
    public int functionId;

    public AsyncMethod getAsyncMethod() {
        return asyncMethod;
    }

    public boolean isReadonly() {
        return readonly;
    }

    public int getFunctionId() {
        return functionId;
    }

    public OriginalMethod getOriginalMethod() {
        return originalMethod;
    }

    public boolean isCellbased() {
        return cellbased;
    }

    public String getMapArgsToInvocation() {
        StringBuffer sb = new StringBuffer();
        int primitiveIndex = 1;
        int referenceIndex = 1;
        for (int argIndex = 0; argIndex < formalArguments.size(); argIndex++) {
            FormalArgument formalArgument = formalArguments.get(argIndex);
            String argType = formalArgument.type;

            if (isCellbased() && argIndex == 0) {
                sb.append("invocation.id").append(" = ").append(formalArgument.name).append(";\n");
            } else if (isPrimtive(argType)) {
                if ("boolean".equals(argType)) {
                    sb.append("invocation.long").append(primitiveIndex)
                            .append(" = ").append(formalArgument.name).append(" ? 1 : 0;\n");
                } else if ("double".equals(argType)) {
                    sb.append("invocation.long").append(primitiveIndex)
                            .append(" = Double.doubleToLongBits(").append(formalArgument.getName()).append(");\n");
                } else if ("float".equals(argType)) {
                    throw new UnsupportedOperationException();
                } else {
                    sb.append("invocation.long").append(primitiveIndex)
                            .append(" = ").append(formalArgument.getName()).append(";\n");
                }
                primitiveIndex++;
            } else {
                sb.append("invocation.reference").append(referenceIndex)
                        .append(" = ").append(formalArgument.getName()).append(";\n");
                referenceIndex++;
            }
        }

        return sb.toString();
    }

    public String getInvocationToArgs() {
        StringBuffer sb = new StringBuffer();
        int primitiveIndex = 1;
        int referenceIndex = 1;
        for (int argIndex = 0; argIndex < formalArguments.size(); argIndex++) {
            FormalArgument formalArgument = formalArguments.get(argIndex);
            String argType = formalArgument.getType();

            if (isCellbased() && argIndex == 0) {
                sb.append("cell");
            } else if (isPrimtive(argType)) {
                if ("boolean".equals(argType)) {
                    sb.append("invocation.long").append(primitiveIndex).append("==1");
                } else if ("double".equals(argType)) {
                    sb.append("Double.longBitsToDouble(invocation.long").append(primitiveIndex).append(")");
                } else if ("float".equals(argType)) {
                    throw new UnsupportedOperationException();
                } else {
                    sb.append("invocation.long").append(primitiveIndex);
                }
                primitiveIndex++;
            } else {
                if (!"Object".equals(argType)) {
                    sb.append("(").append(argType).append(")");
                }
                sb.append("invocation.reference").append(referenceIndex);
                referenceIndex++;
            }

            if (argIndex < formalArguments.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    public String getDeserializedInvocationToArgs() {
        StringBuffer sb = new StringBuffer();
        for (int argIndex = 0; argIndex < formalArguments.size(); argIndex++) {
            FormalArgument formalArgument = formalArguments.get(argIndex);

            String argType = formalArgument.getType();
            if (cellbased && argIndex == 0) {
                sb.append("cell");
            } else {
                if ("long".equals(argType)) {
                    sb.append("in.readLong()");
                } else if ("boolean".equals(argType)) {
                    sb.append("in.readBoolean()");
                } else if ("int".equals(argType)) {
                    sb.append("in.readInteger()");
                } else if ("byte".equals(argType)) {
                    sb.append("in.readByte()");
                } else if ("float".equals(argType)) {
                    sb.append("in.readFloat()");
                } else if ("double".equals(argType)) {
                    sb.append("in.readDouble()");
                } else if ("char".equals(argType)) {
                    sb.append("in.readChar()");
                } else if ("short".equals(argType)) {
                    sb.append("in.readShort()");
                } else {
                    sb.append("(").append(argType).append(")").append("in.readObject()");
                }
            }

            if (argIndex < formalArguments.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    public String getSerializeArgs() {
        StringBuffer sb = new StringBuffer();
        for (int argIndex = 0; argIndex < formalArguments.size(); argIndex++) {
            FormalArgument formalArgument = formalArguments.get(argIndex);
            String arg = formalArgument.getType();

            if (cellbased && argIndex == 0) {

            } else {
                if ("long".equals(arg)) {
                    sb.append("out.writeLong(");
                } else if ("boolean".equals(arg)) {
                    sb.append("out.writeBoolean(");
                } else if ("int".equals(arg)) {
                    sb.append("out.writeInt(");
                } else if ("byte".equals(arg)) {
                    sb.append("out.writeByte(");
                } else if ("float".equals(arg)) {
                    sb.append("out.writeFloat(");
                } else if ("double".equals(arg)) {
                    sb.append("out.writeDouble(");
                } else if ("char".equals(arg)) {
                    sb.append("out.writeChar(");
                } else if ("short".equals(arg)) {
                    sb.append("out.writeShort(");
                } else {
                    sb.append("out.writeObject(");
                }
                sb.append(formalArgument.getName()).append(");\n");
            }
        }
        return sb.toString();
    }

    public String getFunctionConstantName() {
        return "FUNCTION_" + name + formalArguments.size();
    }

    public String getUniqueMethodName() {
        return targetMethod + formalArguments.size();
    }

    public String getReturnTypeAsObject() {
        if ("void".equals(returnType)) {
            return "Void";
        } else if ("long".equals(returnType)) {
            return "Long";
        } else if ("boolean".equals(returnType)) {
            return "Boolean";
        } else if ("int".equals(returnType)) {
            return "Integer";
        } else if ("byte".equals(returnType)) {
            return "Byte";
        } else if ("float".equals(returnType)) {
            return "Float";
        } else if ("double".equals(returnType)) {
            return "Double";
        } else if ("char".equals(returnType)) {
            return "Character";
        } else if ("short".equals(returnType)) {
            return "Short";
        } else {
            return returnType;
        }
    }

    public boolean isVoidReturnType() {
        return "void".equals(returnType);
    }

    public String getTargetMethod() {
        return targetMethod;
    }

    public String getInvocationClassName() {
        return invocationClassName;
    }
}
