package com.flow.saga.aspect;

import com.flow.saga.annotation.SagaMainTransactionProcess;
import com.flow.saga.utils.AnnotationUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Aspect
@Component
public class SagaMainTransactionAspect {

    @Resource
    private RuntimeSagaTransactionManager runtimeSagaTransactionManager;

    @Resource
    private SagaMainTransactionInterceptor sagaMainTransactionInterceptor;

    @Resource
    private SagaMainTransactionRecoverInterceptor sagaMainTransactionRecoverInterceptor;

    @Around(value = "@annotation(com.flow.saga.annotation.SagaMainTransactionProcess)")
    public Object sagaFlowProcess(ProceedingJoinPoint joinPoint) throws Throwable {
        Boolean recover = runtimeSagaTransactionManager.isRecover();

        SagaMainTransactionProcess sagaMainTransactionProcess = AnnotationUtil.findAnnotation(joinPoint, SagaMainTransactionProcess.class);

        //离线恢复
        if (recover) {
            return sagaMainTransactionRecoverInterceptor.intercept(joinPoint, sagaMainTransactionProcess, recover);
        } else { //正常执行
            return sagaMainTransactionInterceptor.intercept(joinPoint, sagaMainTransactionProcess, recover);
        }

    }
}
