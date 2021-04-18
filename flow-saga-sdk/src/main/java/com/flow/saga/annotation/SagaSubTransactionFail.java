package com.flow.saga.annotation;

import java.lang.annotation.*;

/**
 * 子事务失败后回调
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SagaSubTransactionFail {
    String sagaSubTransactionName() default "";
}
