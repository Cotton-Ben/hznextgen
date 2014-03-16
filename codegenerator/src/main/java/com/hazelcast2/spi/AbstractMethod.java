package com.hazelcast2.spi;

import java.util.LinkedList;
import java.util.List;

public class AbstractMethod {
    public String name;
    public String returnType;
    public String debug = "";

    public List<FormalArgument> formalArguments = new LinkedList<FormalArgument>();

    public String getDebug() {
        return debug;
    }

    public boolean getHasOneArgOrMore() {
        return formalArguments.size() > 0;
    }

    public String getName() {
        return name;
    }

    public String getReturnType() {
        return returnType;
    }

    public static boolean isPrimtive(String type) {
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

    public String getActualArguments() {
        StringBuffer sb = new StringBuffer();
        for (int k=0;k<formalArguments.size();k++) {
            sb.append(formalArguments.get(k).getName());
            if (k < formalArguments.size()-1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    public String getFormalArguments() {
        StringBuffer sb = new StringBuffer();
        for (int k = 0; k < formalArguments.size(); k++) {
            FormalArgument arg = formalArguments.get(k);

            sb.append("final ").append(arg.getType()).append(" ").append(arg.getName());

            if (k < formalArguments.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
}
