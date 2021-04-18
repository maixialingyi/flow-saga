package com.flow.saga.annotation;

import java.lang.annotation.*;

/**
 * 子事务成功后回调
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SagaSubTransactionSuccess {
    String sagaSubTransactionName() default "";
}
