package com.flow.saga.annotation;

import java.lang.annotation.*;

/**
 * 子事务
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SagaSubTransactionProcess {
    String sagaSubTransactionName() default "";

    Class<? extends Exception>[] reExecuteExceptions() default {};

    Class<? extends Exception>[] rollbackExceptions() default {};
}
