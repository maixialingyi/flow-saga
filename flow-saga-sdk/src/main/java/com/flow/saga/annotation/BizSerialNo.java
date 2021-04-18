package com.flow.saga.annotation;

import java.lang.annotation.*;

/**
 * 业务流水号标识注解，用于被调用方幂等
 */
@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BizSerialNo {
}
