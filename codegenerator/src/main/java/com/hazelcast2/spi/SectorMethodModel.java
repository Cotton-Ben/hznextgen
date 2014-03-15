package com.hazelcast2.spi;

import java.util.LinkedList;
import java.util.List;

public class SectorMethodModel {
    public String targetMethod;
    public String name;
    public String invocationClassName;
    public String returnType;
    public final List<String> args = new LinkedList<>();
    public boolean readonly = false;
    public boolean cellbased = false;
    //useful while generating code. Has no other meaning then to be able to display some text in generated code.
    public String debug = "";

    public boolean isReadonly() {
        return readonly;
    }

    public String getDebug() {
        return debug;
    }

    public int functionId;

    public int getFunctionId(){
        return functionId;
    }

    public boolean getHasOneArgOrMore(){
        return args.size()>0;
    }

    public boolean isCellbased(){
        return cellbased;
    }

    public String getMapArgsToInvocation() {
        StringBuffer sb = new StringBuffer();
        int primitiveIndex = 1;
        int referenceIndex = 1;
        for (int argIndex = 1; argIndex <= args.size(); argIndex++) {
            String arg = args.get(argIndex - 1);
            if (isPrimtive(arg)) {
                if ("boolean".equals(arg)) {
                    sb.append("invocation.long").append(primitiveIndex).append(" = arg")
                            .append(argIndex).append(" ? 1 : 0;\n");
                } else if ("double".equals(arg)) {
                    sb.append("invocation.long").append(primitiveIndex).append(" = Double.doubleToLongBits(arg")
                            .append(argIndex).append(");\n");
                } else if ("float".equals(arg)) {
                    throw new UnsupportedOperationException();
                } else {
                    sb.append("invocation.long").append(primitiveIndex).append(" = arg").append(argIndex).append(";\n");
                }
                primitiveIndex++;
            } else {
                sb.append("invocation.reference").append(referenceIndex).append(" = arg").append(argIndex).append(";\n");
                referenceIndex++;
            }
        }

        return sb.toString();
    }

    public String getInvocationToArgs() {
        StringBuffer sb = new StringBuffer();
        int primitiveIndex = 1;
        int referenceIndex = 1;
        for (int k = 1; k <= args.size(); k++) {
            String arg = args.get(k - 1);
            if (isPrimtive(arg)) {
                if ("boolean".equals(arg)) {
                    sb.append("invocation.long").append(primitiveIndex).append("==1");
                } else if ("double".equals(arg)) {
                    sb.append("Double.longBitsToDouble(invocation.long").append(primitiveIndex).append(")");
                } else if ("float".equals(arg)) {
                    throw new UnsupportedOperationException();
                } else {
                    sb.append("invocation.long").append(primitiveIndex);
                }
                primitiveIndex++;
            } else {
                if (!"java.lang.Object".equals(arg)) {
                    sb.append("(").append(arg).append(")");
                }
                sb.append("invocation.reference").append(referenceIndex);
                referenceIndex++;
            }

            if (k < args.size()) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    public String getDeserializedInvocationToArgs() {
        StringBuffer sb = new StringBuffer();
        for (int k = 1; k <= args.size(); k++) {
            String arg = args.get(k - 1);
            if ("long".equals(arg)) {
                sb.append("in.readLong()");
            } else if ("boolean".equals(arg)) {
                sb.append("in.readBoolean()");
            } else if ("int".equals(arg)) {
                sb.append("in.readInteger()");
            } else if ("byte".equals(arg)) {
                sb.append("in.readByte()");
            } else if ("float".equals(arg)) {
                sb.append("in.readFloat()");
            } else if ("double".equals(arg)) {
                sb.append("in.readDouble()");
            } else if ("char".equals(arg)) {
                sb.append("in.readChar()");
            } else if ("short".equals(arg)) {
                sb.append("in.readShort()");
            } else {
                sb.append("(").append(arg).append(")").append("in.readObject()");
            }

            if (k < args.size()) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    public String getArgsToSerialize() {
        StringBuffer sb = new StringBuffer();
        for (int k = 1; k <= args.size(); k++) {
            String arg = args.get(k - 1);
            if ("long".equals(arg)) {
                sb.append("out.writeLong(arg").append(k).append(");\n");
            } else if ("boolean".equals(arg)) {
                sb.append("out.writeBoolean(arg").append(k).append(");\n");
            } else if ("int".equals(arg)) {
                sb.append("out.writeInt(arg").append(k).append(");\n");
            } else if ("byte".equals(arg)) {
                sb.append("out.writeByte(arg").append(k).append(");\n");
            } else if ("float".equals(arg)) {
                sb.append("out.writeFloat(arg").append(k).append(");\n");
            } else if ("double".equals(arg)) {
                sb.append("out.writeDouble(arg").append(k).append(");\n");
            } else if ("char".equals(arg)) {
                sb.append("out.writeChar(arg").append(k).append(");\n");
            } else if ("short".equals(arg)) {
                sb.append("out.writeShort(arg").append(k).append(");\n");
            } else {
                sb.append("out.writeObject(arg").append(k).append(");\n");
            }
        }
        return sb.toString();
    }

    public List<String> getPrimitiveArgs() {
        List<String> result = new LinkedList<>();
        for (String arg : args) {
            if (isPrimtive(arg)) {
                result.add(arg);
            }
        }
        return result;
    }

    private static boolean isPrimtive(String type) {
        if ("void".equals(type)) {
            return true;
        } else if ("long".equals(type)) {
            return true;
        } else if ("boolean".equals(type)) {
            return true;
        } else if ("int".equals(type)) {
            return true;
        } else if ("byte".equals(type)) {
            return true;
        } else if ("float".equals(type)) {
            return true;
        } else if ("double".equals(type)) {
            return true;
        } else if ("char".equals(type)) {
            return true;
        } else if ("short".equals(type)) {
            return true;
        } else {
            return false;
        }
    }

    public String getFunctionConstantName() {
        return "FUNCTION_" + name + args.size();
    }

    public String getUniqueMethodName() {
        return name + args.size();
    }

    public String getTrailingComma() {
        return args.size() == 0 ? "" : ", ";
    }

    public String getFormalArguments() {
        StringBuffer sb = new StringBuffer();
        for (int k = 1; k <= args.size(); k++) {
            sb.append("final ").append(args.get(k - 1)).append(" arg").append(k);
            if (k < args.size()) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    public String getActualArguments() {
        StringBuffer sb = new StringBuffer();
        for (int k = 1; k <= args.size(); k++) {
            sb.append("arg" + k);
            if (k < args.size()) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    public String getAsyncName() {
        return "async" + SectorCodeGenerator.capitalizeFirstLetter(getName());
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


    public List<String> getArgs() {
        return args;
    }

    public boolean isVoidReturnType() {
        return "void".equals(returnType);
    }

    public String getReturnType() {
        return returnType;
    }

    public String getTargetMethod() {
        return targetMethod;
    }

    public String getName() {
        return name;
    }

    public String getInvocationClassName() {
        return invocationClassName;
    }
}