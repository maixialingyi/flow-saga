package com.flow.saga.aspect;

import com.flow.saga.annotation.SagaSubTransactionProcess;
import com.flow.saga.entity.SagaSubTransactionEntity;
import com.flow.saga.entity.SagaTransactionContext;
import com.flow.saga.entity.SagaTransactionContextHolder;
import com.flow.saga.entity.SagaTransactionEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RuntimeSagaTransactionRecoverManager extends BaseSagaTransactionHandler {

    public void begin(SagaTransactionContext context) {
        SagaTransactionEntity sagaTransactionEntity = context.getSagaTransactionEntity();
        sagaTransactionEntity.setRecover(true);
        super.updateSagaTransaction(sagaTransactionEntity);
        context.addLayerCount();
        log.debug("[RuntimeSagaTransactionProcess]流程{}恢复开始, 流程类型{}, 业务流水号:{}",
                sagaTransactionEntity.getSagaTransactionName(), sagaTransactionEntity.getSagaTransactionType(),
                sagaTransactionEntity.getBizSerialNo());
    }

    public SagaSubTransactionEntity getCurrentSubTransaction(
            SagaSubTransactionProcess sagaSubTransactionProcess) {
        SagaTransactionContext context = SagaTransactionContextHolder.getSagaTransactionContext();
        SagaTransactionEntity sagaTransactionEntity = context.getSagaTransactionEntity();
        List<SagaSubTransactionEntity> sagaSubTransactionEntities = sagaTransactionEntity
                .getSagaSubTransactionEntities();
        List<SagaSubTransactionEntity> currentSagaSubTransactions = sagaSubTransactionEntities.stream().filter(
                a -> a.getSubTransactionName().equals(sagaSubTransactionProcess.sagaSubTransactionName()))
                .collect(Collectors.toList());
        return CollectionUtils.isEmpty(currentSagaSubTransactions) ? null : currentSagaSubTransactions.get(0);

    }

    public void updateSubTransaction(SagaSubTransactionEntity sagaSubTransactionEntity) {
        this.updateSagaSubTransaction(sagaSubTransactionEntity);
        log.debug("[RuntimeSagaSubTransactionProcess]子流程{}开始, 业务流水号:{}",
                sagaSubTransactionEntity.getSubTransactionName(), sagaSubTransactionEntity.getBizSerialNo());
    }

    public void release(SagaTransactionContext context) {
        // 外层service已清理
    }
}
