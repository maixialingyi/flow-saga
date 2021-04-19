package com.flow.saga.aspect.exceptionhandler;

import com.flow.saga.entity.SagaTransactionContext;
import com.flow.saga.entity.SagaTransactionEntity;
import com.flow.saga.entity.SagaTransactionTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ReExecuteSagaTransactionExceptionHandler extends BaseRuntimeSagaTransactionExceptionHandler
        implements SagaTransactionExceptionHandler {
    @Override
    public void handleException(SagaTransactionContext context, Exception e) {
        SagaTransactionEntity sagaTransactionEntity = context.getSagaTransactionEntity();
        sagaTransactionEntity.fail();

        log.error("[RuntimeSagaTransactionProcess]流程{}, 流程类型{}, 补偿失败, 业务流水号:{}",
                sagaTransactionEntity.getSagaTransactionName(), sagaTransactionEntity.getSagaTransactionType(),
                sagaTransactionEntity.getBizSerialNo());

    }

    @Override
    public SagaTransactionTypeEnum getSagaTransactionType() {
        return SagaTransactionTypeEnum.RE_EXECUTE;
    }
}
