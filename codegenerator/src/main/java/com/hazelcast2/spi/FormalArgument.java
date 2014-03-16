package com.hazelcast2.spi;

public class FormalArgument {

    public String name;
    public String type;

    public String getName() {
        return name;
    }

    public String getType() {
        if(type.equals("java.lang.Object")){
            return "Object";
        }else if(type.equals("java.lang.String")){
            return "String";
        }

        return type;
    }
}
