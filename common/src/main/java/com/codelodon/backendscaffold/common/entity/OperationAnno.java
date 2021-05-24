package com.codelodon.backendscaffold.common.entity;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface OperationAnno {
    String name();

    String httpMethod();

    boolean needAuth() default true;
}
