package com.flow.saga.aspect;

import com.flow.saga.annotation.SagaSubTransactionProcess;
import com.flow.saga.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class SagaSubTransactionRecoverInterceptor {
    @Resource
    private RuntimeSagaTransactionRecoverManager runtimeSagaTransactionRecoverManager;

    public Object intercept(ProceedingJoinPoint joinPoint,
                            SagaSubTransactionProcess sagaSubTransactionProcess) throws Throwable {
        SagaTransactionContext sagaTransactionContext = SagaTransactionContextHolder.getSagaTransactionContext();
        SagaTransactionEntity sagaTransactionEntity = sagaTransactionContext.getSagaTransactionEntity();

        SagaSubTransactionEntity sagaSubTransactionEntity = runtimeSagaTransactionRecoverManager
                .getCurrentSubTransaction(sagaSubTransactionProcess);

        if (sagaSubTransactionEntity != null
                && sagaSubTransactionEntity.getTransactionStatus() == SagaTransactionStatus.SUCCESS.getStatus()) {
            log.debug("[RuntimeSagaSubTransactionProcess]子流程{}已执行成功，跳过执行, 业务流水号:{}",
                    sagaSubTransactionEntity.getSubTransactionName(), sagaSubTransactionEntity.getBizSerialNo());
            return sagaSubTransactionEntity.getAndConstructReturnValue();
        } else if (sagaSubTransactionEntity != null && (sagaSubTransactionEntity
                .getTransactionStatus() == SagaTransactionStatus.ROBACK_SUCCESS.getStatus()
                || sagaSubTransactionEntity.getTransactionStatus() == SagaTransactionStatus.ROBACK_FAIL.getStatus())) {
            log.debug("[RuntimeSagaSubTransactionProcess]子流程{}已执行回滚，跳过执行, 业务流水号:{}",
                    sagaSubTransactionEntity.getSubTransactionName(), sagaSubTransactionEntity.getBizSerialNo());
            return sagaSubTransactionEntity.getAndConstructReturnValue();
        } else if (sagaSubTransactionEntity != null
                && sagaSubTransactionEntity.getTransactionStatus() != SagaTransactionStatus.SUCCESS.getStatus()) {
            // 更新状态为初始，进行重试
            sagaSubTransactionEntity.initStatus();
            // 填充运行时动态信息
            sagaSubTransactionEntity.setRollbackExceptions(sagaSubTransactionProcess.rollbackExceptions());
            sagaSubTransactionEntity.setCompensateExceptions(sagaSubTransactionProcess.compensateExceptions());
            sagaTransactionContext.setCurrentSagaSubTransaction(sagaSubTransactionEntity);
            runtimeSagaTransactionRecoverManager.updateSubTransaction(sagaSubTransactionEntity);
        } else {
            sagaSubTransactionEntity = SagaSubTransactionEntityInitFactory.initSagaSubTransactionEntity(joinPoint, sagaSubTransactionProcess,
                    sagaTransactionEntity);
            runtimeSagaTransactionRecoverManager.addSubTransaction(sagaTransactionContext, sagaSubTransactionEntity);
        }

        try {
            Object object = joinPoint.proceed();
            runtimeSagaTransactionRecoverManager.commitSubTransaction(sagaSubTransactionEntity, object);
            runtimeSagaTransactionRecoverManager.successSubTransactionProcess(sagaSubTransactionEntity);
            return object;
        } catch (Exception e) {
            try {
                Object object = runtimeSagaTransactionRecoverManager
                        .handleSubTransactionException(sagaTransactionContext, sagaSubTransactionEntity, joinPoint, e);
                runtimeSagaTransactionRecoverManager.commitSubTransaction(sagaSubTransactionEntity, object);
                runtimeSagaTransactionRecoverManager.successSubTransactionProcess(sagaSubTransactionEntity);
                return object;
            } catch (Exception e1) {
                runtimeSagaTransactionRecoverManager.failSubTransactionProcess(sagaSubTransactionEntity, e1);
                throw e1;
            }

        }
    }
}
