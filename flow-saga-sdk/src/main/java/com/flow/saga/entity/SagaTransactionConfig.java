package com.flow.saga.entity;

import lombok.Data;
import lombok.ToString;

/**
 * SagaTransactionProcess
 */
@Data
@ToString(callSuper = true)
public class SagaTransactionConfig {

    private SagaTransactionTypeEnum sagaTransactionType;

    // 补偿的重试次数
    private int retryTime;

    // 补偿的重试时间间隔，单位秒
    private long retryInterval;

    private Class<? extends Exception>[] reExecuteExceptionList;

    private Class<? extends Exception>[] rollbackExceptionList;

    private String startCompensateAfterTransactionName = "";

}
