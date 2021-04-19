package com.flow.saga.aspect;

import com.flow.saga.annotation.SagaSubTransactionProcess;
import com.flow.saga.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;

@Slf4j
@Service
public class SagaSubTransactionInterceptor {
    @Resource
    private RuntimeSagaTransactionManager runtimeSagaTransactionManager;

    public Object intercept(ProceedingJoinPoint joinPoint,
                            SagaSubTransactionProcess runtimeSagaSubTransactionProcess) throws Throwable {

        SagaTransactionContext context = SagaTransactionContextHolder.getSagaTransactionContext();
        if (context == null) {
            return joinPoint.proceed();
        }

        SagaTransactionEntity sagaTransactionEntity = context.getSagaTransactionEntity();

        SagaSubTransactionEntity sagaSubTransactionEntity = SagaSubTransactionEntityInitFactory.initSagaSubTransactionEntity(joinPoint,
                runtimeSagaSubTransactionProcess, sagaTransactionEntity);

        runtimeSagaTransactionManager.addSubTransaction(context, sagaSubTransactionEntity);

        try {
            Object object = joinPoint.proceed();
            runtimeSagaTransactionManager.commitSubTransaction(sagaSubTransactionEntity, object);
            runtimeSagaTransactionManager.successSubTransactionProcess(sagaSubTransactionEntity);
            return object;
        } catch (Exception e) {
            try {
                Object object = runtimeSagaTransactionManager.handleSubTransactionException(context,
                        sagaSubTransactionEntity, joinPoint, e);
                runtimeSagaTransactionManager.commitSubTransaction(sagaSubTransactionEntity, object);
                runtimeSagaTransactionManager.successSubTransactionProcess(sagaSubTransactionEntity);
                return object;
            } catch (Exception e1) {
                runtimeSagaTransactionManager.failSubTransactionProcess(sagaSubTransactionEntity, e1);
                throw e1;
            }
        }
    }
}
