package com.hazelcast2.spi.foo2;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD})
public @interface Foo2OperationMethod {
    boolean readonly() default false;
}
