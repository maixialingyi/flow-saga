package com.flow.saga.aspect.exceptionhandler;

import com.flow.saga.entity.SagaTransactionContext;
import com.flow.saga.entity.SagaTransactionEntity;
import com.flow.saga.entity.SagaTransactionTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RollbackRuntimeSagaTransactionExceptionHandler extends BaseRuntimeSagaTransactionExceptionHandler
        implements RuntimeSagaTransactionExceptionHandler {
    @Override
    public void handleException(SagaTransactionContext sagaTransactionContext, Exception e) {
        SagaTransactionEntity sagaTransactionEntity = sagaTransactionContext.getSagaTransactionEntity();
        log.debug("[RuntimeSagaTransactionProcess]流程{}, 流程类型{}, 处理回滚开始,业务流水号:{}",
                sagaTransactionEntity.getSagaTransactionName(), sagaTransactionEntity.getSagaTransactionType(),
                sagaTransactionEntity.getBizSerialNo());
        super.handleRollback(sagaTransactionEntity, e);
        log.debug("[RuntimeSagaTransactionProcess]流程{}, 流程类型{}, 处理回滚结束,业务流水号:{}",
                sagaTransactionEntity.getSagaTransactionName(), sagaTransactionEntity.getSagaTransactionType(),
                sagaTransactionEntity.getBizSerialNo());
    }

    @Override
    public SagaTransactionTypeEnum getSagaTransactionType() {
        return SagaTransactionTypeEnum.ROLLBACK;
    }
}
