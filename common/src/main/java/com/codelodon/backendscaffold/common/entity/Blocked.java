package com.codelodon.backendscaffold.common.entity;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Blocked {
}
