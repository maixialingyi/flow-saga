package com.flow.saga.annotation;


import com.flow.saga.entity.SagaTransactionTypeEnum;
import com.flow.saga.exception.SagaTransactionCompensateException;
import com.flow.saga.exception.SagaTransactionRollbackException;

import java.lang.annotation.*;

/**
 * 主事务
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SagaMainTransactionProcess {

    //事务名称
    String sagaTransactionName() default "";

    //事务失败后处理类型
    SagaTransactionTypeEnum sagaTransactionType() default SagaTransactionTypeEnum.CONFIG_BY_EXCEPTION;

    // 补偿的重试次数
    int retryTime() default 1;

    // 补偿的重试时间间隔，单位毫秒
    long retryInterval() default 1;

    // saga自身异常，导致重试
    Class<? extends Exception>[] compensateExceptions() default { SagaTransactionCompensateException.class };

    // saga自身异常，导致回滚
    Class<? extends Exception>[] rollbackExceptions() default { SagaTransactionRollbackException.class };

    String startCompensateAfterTransactionName() default "";

}
