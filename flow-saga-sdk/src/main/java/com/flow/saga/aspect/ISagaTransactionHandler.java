package com.flow.saga.aspect;

import com.flow.saga.entity.SagaTransactionContext;

public interface ISagaTransactionHandler {

    void begin(SagaTransactionContext context);

    void commit(SagaTransactionContext context);

    void release(SagaTransactionContext context);

}
