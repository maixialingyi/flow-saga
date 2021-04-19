package com.flow.saga.annotation;

import java.lang.annotation.*;

/**
 * 主事务成功后回调
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SagaMainTransactionSuccess {
    String sagaTransactionName() default "";
}
