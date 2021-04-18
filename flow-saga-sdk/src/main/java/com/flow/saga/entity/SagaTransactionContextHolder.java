package com.flow.saga.entity;

public class SagaTransactionContextHolder {

    private static ThreadLocal<SagaTransactionContext> sagaTransactionContextHolder = new InheritableThreadLocal<>();

    public static void putSagaTransactionContext(SagaTransactionContext sagaTransactionContext) {
        sagaTransactionContextHolder.set(sagaTransactionContext);
    }

    public static SagaTransactionContext getSagaTransactionContext() {
        return sagaTransactionContextHolder.get();
    }

    public static void clearSagaTransactionContext() {
        sagaTransactionContextHolder.remove();
    }
}
