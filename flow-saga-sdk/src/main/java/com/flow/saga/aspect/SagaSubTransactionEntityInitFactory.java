package com.flow.saga.aspect;

import com.flow.saga.annotation.SagaSubTransactionFail;
import com.flow.saga.annotation.SagaSubTransactionProcess;
import com.flow.saga.annotation.SagaSubTransactionRollback;
import com.flow.saga.annotation.SagaSubTransactionSuccess;
import com.flow.saga.entity.InvocationContext;
import com.flow.saga.entity.SagaSubTransactionEntity;
import com.flow.saga.entity.SagaTransactionEntity;
import com.flow.saga.entity.SagaTransactionStatus;
import com.flow.saga.utils.JsonUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SagaSubTransactionEntityInitFactory {

    public static SagaSubTransactionEntity initSagaSubTransactionEntity(ProceedingJoinPoint joinPoint,
                                                          SagaSubTransactionProcess sagaSubTransactionProcess,
                                                          SagaTransactionEntity sagaTransactionEntity) {

        String subTransactionName = sagaSubTransactionProcess.sagaSubTransactionName();

        // 方法参数类型List
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        List<String> methodParamTypeList = Arrays.stream(methodSignature.getParameterTypes()).map(Class::getName)
                .collect(Collectors.toList());

        // 参数类型List
        List<String> paramTypeList = new ArrayList<>();
        // 参数值的List
        List<String> paramJsonList = new ArrayList<>();

        for (int i = 0; i < joinPoint.getArgs().length; i++) {
            Object arg = joinPoint.getArgs()[i];
            paramJsonList.add(JsonUtil.toJson(arg));
            paramTypeList.add(null == arg ? methodParamTypeList.get(i) : arg.getClass().getName());
        }

        InvocationContext successInvocationContext = null;
        InvocationContext failInvocationContext = null;
        InvocationContext rollbackInvocationContext = null;

        Class<?> targetClass = joinPoint.getTarget().getClass();
        for (Method method : targetClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(SagaSubTransactionSuccess.class)) {
                SagaSubTransactionSuccess runtimeSagaSubTransactionSuccess = method
                        .getAnnotation(SagaSubTransactionSuccess.class);
                if (runtimeSagaSubTransactionSuccess.sagaSubTransactionName().equals(subTransactionName)) {
                    successInvocationContext = new InvocationContext(targetClass, method, method.getParameterTypes());
                }

            }
            if (method.isAnnotationPresent(SagaSubTransactionFail.class)) {
                SagaSubTransactionFail runtimeSagaSubTransactionFail = method
                        .getAnnotation(SagaSubTransactionFail.class);
                if (runtimeSagaSubTransactionFail.sagaSubTransactionName().equals(subTransactionName)) {
                    failInvocationContext = new InvocationContext(targetClass, method, method.getParameterTypes());
                }

            }
            if (method.isAnnotationPresent(SagaSubTransactionRollback.class)) {
                SagaSubTransactionRollback runtimeSagaSubTransactionRollback = method
                        .getAnnotation(SagaSubTransactionRollback.class);
                if (runtimeSagaSubTransactionRollback.sagaSubTransactionName().equals(subTransactionName)) {
                    rollbackInvocationContext = new InvocationContext(targetClass, method, method.getParameterTypes());
                }

            }
        }

        SagaSubTransactionEntity sagaSubTransactionEntity = new SagaSubTransactionEntity();
        sagaSubTransactionEntity.setShardRoutingKey(sagaTransactionEntity.getShardRoutingKey());
        sagaSubTransactionEntity.setSubTransactionName(subTransactionName);
        sagaSubTransactionEntity.setSagaTransactionId(sagaTransactionEntity.getId());
        sagaSubTransactionEntity.setBizSerialNo(sagaTransactionEntity.getBizSerialNo());
        sagaSubTransactionEntity.setSubTransactionName(sagaSubTransactionProcess.sagaSubTransactionName());
        sagaSubTransactionEntity.setTransactionStatus(SagaTransactionStatus.INIT.getStatus());
        sagaSubTransactionEntity.setSubTransactionClassName(targetClass.getName());
        sagaSubTransactionEntity.setSubTransactionClassMethodName(methodSignature.getMethod().getName());

        sagaSubTransactionEntity.setParamValueJson(JsonUtil.toJson(paramJsonList));
        sagaSubTransactionEntity.setParamTypeJson(JsonUtil.toJson(paramTypeList));
        sagaSubTransactionEntity.setParamTypes(methodSignature.getParameterTypes());
        sagaSubTransactionEntity.setParamValues(joinPoint.getArgs());
        sagaSubTransactionEntity.setReturnTypeJson(methodSignature.getReturnType().getName());

        sagaSubTransactionEntity.setCompensateExceptions(sagaSubTransactionProcess.reExecuteExceptions());
        sagaSubTransactionEntity.setRollbackExceptions(sagaSubTransactionProcess.rollbackExceptions());

        sagaSubTransactionEntity.setSuccessInvocationContext(successInvocationContext);
        sagaSubTransactionEntity.setFailInvocationContext(failInvocationContext);
        sagaSubTransactionEntity.setRollbackInvocationContext(rollbackInvocationContext);

        return sagaSubTransactionEntity;
    }

}
