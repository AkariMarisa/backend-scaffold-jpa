package com.codelodon.backendscaffold.common.entity;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ModuleAnno {
    String name();
}
