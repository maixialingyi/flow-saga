package com.flow.saga.aspect;

import com.flow.saga.annotation.SagaMainTransactionProcess;
import com.flow.saga.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


@Slf4j
@Component
public class SagaMainTransactionRecoverInterceptor {

    @Resource
    private RuntimeSagaTransactionRecoverManager runtimeSagaTransactionRecoverManager;

    public Object intercept(ProceedingJoinPoint joinPoint, SagaMainTransactionProcess sagaMainTransactionProcess,
                            boolean recover) throws Throwable {

        // 恢复模式，线程上下文已经设置
        SagaTransactionContext sagaTransactionContext = SagaTransactionContextHolder.getSagaTransactionContext();
        this.recoverSagaTransactionContext(sagaTransactionContext, sagaMainTransactionProcess, recover);

        try {
            // 恢复事务上下文，开始恢复事务
            runtimeSagaTransactionRecoverManager.begin(sagaTransactionContext);
            Object returnValue = joinPoint.proceed();
            // 恢复提交事务
            runtimeSagaTransactionRecoverManager.commit(sagaTransactionContext);
            return returnValue;
        } catch (Throwable e) {
            if (e instanceof Exception) {
                // 处理事务回滚
                runtimeSagaTransactionRecoverManager.handleException(sagaTransactionContext, (Exception) e);
            } else {
                runtimeSagaTransactionRecoverManager.handleException(sagaTransactionContext, new Exception(e));
            }
            throw e;
        } finally {
            // 释放事务
            runtimeSagaTransactionRecoverManager.release(sagaTransactionContext);
        }

    }

    void recoverSagaTransactionContext(SagaTransactionContext sagaTransactionContext,
                                       SagaMainTransactionProcess sagaMainTransactionProcess, Boolean recover) {
        SagaTransactionEntity sagaTransactionEntity = sagaTransactionContext.getSagaTransactionEntity();

        // 重置transaction 运行时信息
        sagaTransactionEntity.setTransactionStatus(SagaTransactionStatus.INIT.getStatus());
        sagaTransactionEntity.setRecover(recover);
        sagaTransactionEntity.setRetryTime(0);

        SagaTransactionConfig sagaTransactionConfig = new SagaTransactionConfig();
        sagaTransactionConfig.setSagaTransactionType(sagaMainTransactionProcess.sagaTransactionType());
        sagaTransactionConfig.setStartCompensateAfterTransactionName(
                sagaMainTransactionProcess.startCompensateAfterTransactionName());
        sagaTransactionConfig.setReExecuteExceptionList(sagaMainTransactionProcess.compensateExceptions());
        sagaTransactionConfig.setRollbackExceptionList(sagaMainTransactionProcess.rollbackExceptions());
        sagaTransactionConfig.setRetryTime(sagaMainTransactionProcess.retryTime());
        sagaTransactionConfig.setRetryInterval(sagaMainTransactionProcess.retryInterval());

        sagaTransactionContext.setSagaTransactionConfig(sagaTransactionConfig);
    }

}
