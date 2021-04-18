package com.flow.saga.aspect;

import com.flow.saga.annotation.SagaSubTransactionProcess;
import com.flow.saga.utils.AnnotationUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Aspect
@Service
public class RuntimeSagaSubTransactionAspect {
    @Resource
    private RuntimeSagaTransactionManager runtimeSagaTransactionManager;
    @Resource
    private RuntimeSubTransactionInterceptor runtimeSubTransactionInterceptor;
    //@Resource
    //private RuntimeSubTransactionRecoverInterceptor runtimeSubTransactionRecoverInterceptor;

    @Around(value = "@annotation(com.flow.saga.annotation.SagaSubTransactionProcess)")
    public Object sagaSubTransactionProcess(ProceedingJoinPoint joinPoint) throws Throwable {
        // 该方法不在saga flow中，直接返回
        if (!runtimeSagaTransactionManager.isInSagaTransaction()) {
            return joinPoint.proceed();
        }
        SagaSubTransactionProcess runtimeSagaSubTransactionProcess = AnnotationUtil.findAnnotation(joinPoint,
                SagaSubTransactionProcess.class);
        Boolean recover = runtimeSagaTransactionManager.isRecover();

        if (recover) {
            //return runtimeSubTransactionRecoverInterceptor.intercept(joinPoint, runtimeSagaSubTransactionProcess);
            return null;
        } else {
            return runtimeSubTransactionInterceptor.intercept(joinPoint, runtimeSagaSubTransactionProcess);
        }
    }


}
