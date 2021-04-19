package com.flow.saga.aspect.exceptionhandler;

import com.flow.saga.entity.SagaTransactionContext;
import com.flow.saga.entity.SagaTransactionEntity;
import com.flow.saga.entity.SagaTransactionTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ConfigByExceptionSagaTransactionExceptionHandler extends BaseRuntimeSagaTransactionExceptionHandler
        implements SagaTransactionExceptionHandler {
    @Override
    public void handleException(SagaTransactionContext context, Exception e) {
        SagaTransactionEntity sagaTransactionEntity = context.getSagaTransactionEntity();

        // 回滚
        if (context.needRollback(e)) {
            log.debug("[RuntimeSagaTransactionProcess]流程{}, 流程类型{}, 根据回滚异常处理回滚开始,业务流水号:{}",
                    sagaTransactionEntity.getSagaTransactionName(), sagaTransactionEntity.getSagaTransactionType(),
                    sagaTransactionEntity.getBizSerialNo());

            super.handleRollback(sagaTransactionEntity, e);

            log.debug("[RuntimeSagaTransactionProcess]流程{}, 流程类型{}, 根据回滚异常处理回滚结束,业务流水号:{}",
                    sagaTransactionEntity.getSagaTransactionName(), sagaTransactionEntity.getSagaTransactionType(),
                    sagaTransactionEntity.getBizSerialNo());

        } else if (context.isCompensateExceptionType(e)) {//重试
            sagaTransactionEntity.fail();
            log.error("[RuntimeSagaTransactionProcess]流程{}, 流程类型{}, 根据补偿异常补偿失败,业务流水号:{}",
                    sagaTransactionEntity.getSagaTransactionName(), sagaTransactionEntity.getSagaTransactionType(),
                    sagaTransactionEntity.getBizSerialNo());
        } else {
            // 原则上不应该走到这个分支，如果走到这个分支，说明业务没有设置正确saga事务类型，需要注意
            log.error("[RuntimeSagaTransactionProcess]流程{}, 流程类型{}, 根据异常配置处理, 未配置回滚以及补偿异常, 业务流水号:{}",
                    sagaTransactionEntity.getSagaTransactionName(), sagaTransactionEntity.getSagaTransactionType(),
                    sagaTransactionEntity.getBizSerialNo());
        }

    }

    @Override
    public SagaTransactionTypeEnum getSagaTransactionType() {
        return SagaTransactionTypeEnum.CONFIG_BY_EXCEPTION;
    }

}
