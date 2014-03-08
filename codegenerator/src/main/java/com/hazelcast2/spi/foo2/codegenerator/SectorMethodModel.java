package com.hazelcast2.spi.foo2.codegenerator;

import java.util.LinkedList;
import java.util.List;

public class SectorMethodModel {
    public String targetMethod;
    public String name;
    public String invocationClassName;
    public String returnType;
    public final List<String> args = new LinkedList<>();

    public String getMapArgsToInvocation() {
        List<String> primtiveArgs = getPrimitiveArgs();
        StringBuffer sb = new StringBuffer();
        for (int k = 0; k < primtiveArgs.size(); k++) {
            String arg = primtiveArgs.get(k);
            if ("long".equals(arg)) {
                sb.append("invocation.long").append(k+1).append(" = arg").append(k).append(";\n");
            } else if ("boolean".equals(arg)) {
                sb.append("invocation.long").append(k+1).append(" = arg").append(k).append(" ? 1 : 0;\n");
            } else if ("int".equals(arg)) {
                sb.append("invocation.long").append(k+1).append(" = arg0").append(k).append(";\n");
            } else if ("byte".equals(arg)) {
                sb.append("invocation.long").append(k+1).append(" = arg0").append(k).append(";\n");
            } else if ("float".equals(arg)) {
                sb.append("invocation.long").append(k+1).append(" = arg0").append(k).append(";\n");
            } else if ("double".equals(arg)) {
                sb.append("invocation.long").append(k+1).append(" = arg0").append(k).append(";\n");;
            } else if ("char".equals(arg)) {
                sb.append("invocation.long").append(k+1).append(" = arg0").append(k).append(";\n");
            } else if ("short".equals(arg)) {
                sb.append("invocation.long").append(k+1).append(" = arg0").append(k).append(";\n");
            } else {
                throw new RuntimeException();
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

    public String getInvocationToArgs() {
        List<String> primtiveArgs = getPrimitiveArgs();
        StringBuffer sb = new StringBuffer();
        for (int k = 1; k <= primtiveArgs.size(); k++) {
            String arg = primtiveArgs.get(k-1);
            if ("long".equals(arg)) {
                sb.append("invocation.long").append(k);
            } else if ("boolean".equals(arg)) {
                sb.append("invocation.long").append(k).append("==1");
            } else if ("int".equals(arg)) {
                sb.append("invocation.long").append(k);
            } else if ("byte".equals(arg)) {
                sb.append("invocation.long").append(k);
            } else if ("float".equals(arg)) {
                sb.append("invocation.long").append(k);
            } else if ("double".equals(arg)) {
                sb.append("invocation.long").append(k);
            } else if ("char".equals(arg)) {
                sb.append("invocation.long").append(k);
            } else if ("short".equals(arg)) {
                sb.append("invocation.long").append(k);
            } else {
                throw new RuntimeException();
            }

            if(k<primtiveArgs.size()) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    public String getFunctionConstantName() {
        return "FUNCTION_" + name + args.size();
    }

    public String getTrailingComma() {
        return args.size() == 0 ? "" : ", ";
    }

    public String getFormalArguments() {
        StringBuffer sb = new StringBuffer();
        for (int k = 0; k < args.size(); k++) {
            sb.append("final ").append(args.get(k)).append(" arg").append(k);
            if (k < args.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    public String getActualArguments() {
        StringBuffer sb = new StringBuffer();
        for (int k = 0; k < args.size(); k++) {
            sb.append("arg" + k);
            if (k < args.size() - 1) {
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
