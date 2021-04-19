package com.flow.saga.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SagaTransactionContext {
    /**
     * sagaTransactionEntity
     */
    private SagaTransactionEntity sagaTransactionEntity;
    /**
     * SagaTransactionConfig
     */
    private SagaTransactionConfig sagaTransactionConfig;
    /**
     * 是否恢复模式
     */
    private boolean recover;

    /**
     * 事务嵌套的层级数
     */
    private int layerCount;
    /**
     * 当前执行的SagaSubTransaction
     */
    private SagaSubTransactionEntity currentSagaSubTransaction;

    /**
     * 进入事务
     */
    public void addLayerCount() {
        layerCount++;
    }

    /**
     * 退出一个事务
     */
    public void reduceLayerCount() {
        layerCount--;
    }

    /**
     * 判断是否需要重试，如果需要，则增加重试次数
     */
    public boolean needRetryCheckAndAddRetryTime(Exception e) {
        // 先判断当前子事务的配置
        if (this.getCurrentSagaSubTransaction() != null
                && this.getCurrentSagaSubTransaction().getCompensateExceptions() != null
                && this.getCurrentSagaSubTransaction().getCompensateExceptions().length != 0) {

            return this.sagaTransactionEntity.needRetryCheckAndAddRetryTime(
                    this.getCurrentSagaSubTransaction().getCompensateExceptions(),
                    e,
                    this.sagaTransactionConfig.getRetryTime());
        }

        return this.sagaTransactionEntity.needRetryCheckAndAddRetryTime(
                this.sagaTransactionConfig.getReExecuteExceptionList(),
                e,
                this.sagaTransactionConfig.getRetryTime());
    }

    /** 判断是否需要重试 */
    public boolean isCompensateExceptionType(Exception e) {
        // 先判断子事务的配置
        if (this.getCurrentSagaSubTransaction() != null
                && this.getCurrentSagaSubTransaction().getCompensateExceptions() != null
                && this.getCurrentSagaSubTransaction().getCompensateExceptions().length != 0) {

            return this.sagaTransactionEntity.isCompensateExceptionType(
                    this.getCurrentSagaSubTransaction().getCompensateExceptions(),e);
        }

        return this.sagaTransactionEntity.isCompensateExceptionType(
                this.sagaTransactionConfig.getReExecuteExceptionList(),e);
    }


    /** 判断是否要回滚 */
    public boolean needRollback(Exception e) {
        // 先判断子事务的配置
        if (this.getCurrentSagaSubTransaction() != null
                && this.getCurrentSagaSubTransaction().getRollbackExceptions() != null
                && this.getCurrentSagaSubTransaction().getRollbackExceptions().length != 0) {

            return this.sagaTransactionEntity.needRollback(this.getCurrentSagaSubTransaction().getRollbackExceptions(), e);
        }
        return this.sagaTransactionEntity.needRollback(this.sagaTransactionConfig.getRollbackExceptionList(), e);
    }
}
