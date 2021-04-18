package com.flow.saga.annotation;

import java.lang.annotation.*;

/**
 * 子事务回滚
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SagaSubTransactionRollback {
    String sagaSubTransactionName() default "";
}
