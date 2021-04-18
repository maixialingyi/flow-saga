package com.flow.saga.annotation;

import java.lang.annotation.*;

/**
 * 主事务回滚
 */

@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SagaTransactionRollback {
    String sagaTransactionName() default "";
}

