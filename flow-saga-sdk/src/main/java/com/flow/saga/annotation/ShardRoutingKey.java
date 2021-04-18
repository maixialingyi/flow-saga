package com.flow.saga.annotation;

import java.lang.annotation.*;

/**
 * 分库分表健
 */
@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ShardRoutingKey {
}
