package com.flow.saga.aspect;

import com.flow.saga.annotation.SagaTransactionProcess;
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
public class SagaTransactionAspect {

    @Resource
    private RuntimeSagaTransactionManager runtimeSagaTransactionManager;

    @Resource
    private RuntimeTransactionInterceptor runtimeTransactionInterceptor;

    //@Resource
    //private RuntimeTransactionRecoverInterceptor runtimeTransactionRecoverInterceptor;

    @Around(value = "@annotation(com.flow.saga.annotation.SagaTransactionRollback)")
    public Object sagaFlowProcess(ProceedingJoinPoint joinPoint) throws Throwable {
        Boolean recover = runtimeSagaTransactionManager.isRecover();

        SagaTransactionProcess sagaTransactionProcess = AnnotationUtil.findAnnotation(joinPoint, SagaTransactionProcess.class);

        //离线恢复
        if (recover) {
            //return runtimeTransactionRecoverInterceptor.intercept(joinPoint, sagaTransactionProcess, recover);
            return null;
        } else { //正常执行
            return runtimeTransactionInterceptor.intercept(joinPoint, sagaTransactionProcess, recover);
        }

    }
}
