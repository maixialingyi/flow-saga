package com.flow.saga.aspect;

import com.flow.saga.annotation.SagaSubTransactionProcess;
import com.flow.saga.aspect.Manager.SagaTransactionManager;
import com.flow.saga.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;

@Slf4j
@Service
public class SagaSubTransactionInterceptor {
    @Resource
    private SagaTransactionManager sagaTransactionManager;

    public Object intercept(ProceedingJoinPoint joinPoint,
                            SagaSubTransactionProcess runtimeSagaSubTransactionProcess) throws Throwable {

        SagaTransactionContext context = SagaTransactionContextHolder.getSagaTransactionContext();
        if (context == null) {
            return joinPoint.proceed();
        }

        SagaTransactionEntity sagaTransactionEntity = context.getSagaTransactionEntity();

        SagaSubTransactionEntity sagaSubTransactionEntity = SagaSubTransactionEntityInitFactory.initSagaSubTransactionEntity(joinPoint,
                runtimeSagaSubTransactionProcess, sagaTransactionEntity);

        sagaTransactionManager.addSubTransaction(context, sagaSubTransactionEntity);

        try {
            Object object = joinPoint.proceed();
            sagaTransactionManager.commitSubTransaction(sagaSubTransactionEntity, object);
            sagaTransactionManager.successSubTransactionProcess(sagaSubTransactionEntity);
            return object;
        } catch (Exception e) {
            try {
                Object object = sagaTransactionManager.handleSubTransactionException(context,
                        sagaSubTransactionEntity, joinPoint, e);
                sagaTransactionManager.commitSubTransaction(sagaSubTransactionEntity, object);
                sagaTransactionManager.successSubTransactionProcess(sagaSubTransactionEntity);
                return object;
            } catch (Exception e1) {
                sagaTransactionManager.failSubTransactionProcess(sagaSubTransactionEntity, e1);
                throw e1;
            }
        }
    }
}
