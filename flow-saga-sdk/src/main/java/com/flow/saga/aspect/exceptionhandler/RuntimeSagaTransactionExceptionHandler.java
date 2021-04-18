package com.flow.saga.aspect.exceptionhandler;

import com.flow.saga.entity.SagaTransactionContext;
import com.flow.saga.entity.SagaTransactionTypeEnum;

public interface RuntimeSagaTransactionExceptionHandler {
    void handleException(SagaTransactionContext context, Exception e);

    SagaTransactionTypeEnum getSagaTransactionType();
}
