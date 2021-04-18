package com.flow.saga.entity;

public enum SagaTransactionStatus {
    /**
     * 初始态
     */
    INIT(0),
    /**
     * 流程执行成功
     */
    SUCCESS(1),
    /**
     * 流程执行失败
     */
    FAIL(2),
    /**
     * 流程回滚成功
     */
    ROBACK_SUCCESS(3),
    /**
     * 流程回滚失败
     */
    ROBACK_FAIL(4);

    private final int status;

    SagaTransactionStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

}
