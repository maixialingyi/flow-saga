package com.flow.saga.aspect;

import com.flow.saga.annotation.*;
import com.flow.saga.entity.*;
import com.flow.saga.utils.JsonUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class RuntimeTransactionInterceptor{

    @Resource
    private RuntimeSagaTransactionManager runtimeSagaTransactionManager;

    public Object intercept(ProceedingJoinPoint joinPoint, SagaTransactionProcess runtimeSagaTransactionProcess, boolean recover) throws Throwable {

        // 初始化上下文
        SagaTransactionContext sagaTransactionContext = this.initSagaTransactionContext(joinPoint, runtimeSagaTransactionProcess, recover);

        try {
            // 开始事务，保存saga log
            runtimeSagaTransactionManager.begin(sagaTransactionContext);
            // 执行业务流程
            Object returnValue = joinPoint.proceed();
            // 提交事务
            runtimeSagaTransactionManager.commit(sagaTransactionContext);
            return returnValue;
        } catch (Throwable e) {
            if (e instanceof Exception) {
                // 处理事务回滚
                runtimeSagaTransactionManager.handleException(sagaTransactionContext, (Exception) e);
            } else {
                runtimeSagaTransactionManager.handleException(sagaTransactionContext,  new Exception(e));
            }
            throw e;
        } finally {
            // 释放事务
            runtimeSagaTransactionManager.release(sagaTransactionContext);
        }
    }

    /**
     * 初始化上下文
     */
    private SagaTransactionContext initSagaTransactionContext(ProceedingJoinPoint joinPoint,
                         SagaTransactionProcess runtimeSagaTransactionProcess, Boolean recover) {

        // 默认传播行为PROPAGATION_REQUIRED（如果当前没有事务，就新建一个事务，如果已经存在一个事务中，加入到这个事务中）
        SagaTransactionContext sagaTransactionContext = SagaTransactionContextHolder.getSagaTransactionContext();
        if (sagaTransactionContext != null) {
            return sagaTransactionContext;
        }

        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        // 方法参数类型List
        List<String> methodParamTypeList = Arrays.stream(methodSignature.getParameterTypes()).map(Class::getName)
                .collect(Collectors.toList());
        // 参数类型List
        List<String> paramTypeList = new ArrayList<>();
        // 参数值的List
        List<String> paramJsonList = new ArrayList<>();

        // 获取业务流水号、获取参数类型、获取参数值
        String bizSerialNo = "";
        try {
            Object value = getAnnotationStrFromParam(joinPoint, methodSignature, BizSerialNo.class);
            bizSerialNo = value != null ? String.valueOf(value) : "";
        } catch (IllegalAccessException e) {
            log.warn("[RuntimeSagaTransactionProcess]流程{}获取业务流水号失败, 流程类型{}, 业务流水号:{}",
                    runtimeSagaTransactionProcess.sagaTransactionName(),
                    runtimeSagaTransactionProcess.sagaTransactionType().getType(), bizSerialNo, e);
        }
        Long shardRoutingKey = 0L;
        try {
            Object value = getAnnotationStrFromParam(joinPoint, methodSignature, ShardRoutingKey.class);
            shardRoutingKey = value != null ? (long) value : 0L;
        } catch (IllegalAccessException e) {
            log.warn("[RuntimeSagaTransactionProcess]流程{}获取分库分表路由键失败, 流程类型{}, 业务流水号:{}",
                    runtimeSagaTransactionProcess.sagaTransactionName(),
                    runtimeSagaTransactionProcess.sagaTransactionType().getType(), bizSerialNo, e);
        }
        if (joinPoint.getArgs() != null) {
            for (int i = 0; i < joinPoint.getArgs().length; i++) {
                Object arg = joinPoint.getArgs()[i];
                paramJsonList.add(JsonUtil.toJson(arg));
                paramTypeList.add(null == arg ? methodParamTypeList.get(i) : arg.getClass().getName());
            }
        }

        String sagaTransactionName = runtimeSagaTransactionProcess.sagaTransactionName();
        Class<?> targetClass = joinPoint.getTarget().getClass();
        Method[] methods = targetClass.getDeclaredMethods();

        // 构建业务成功回调方法和业务失败回调方法
        InvocationContext successInvocationContext = null;
        InvocationContext failInvocationContext = null;
        InvocationContext rollbackInvocationContext = null;
        for (Method method : methods) {
            if (method.isAnnotationPresent(SagaTransactionSuccess.class)) {
                SagaTransactionSuccess runtimeSagaTransactionSuccess = method
                        .getAnnotation(SagaTransactionSuccess.class);
                if (runtimeSagaTransactionSuccess.sagaTransactionName().equals(sagaTransactionName)) {
                    successInvocationContext = new InvocationContext(targetClass, method, method.getParameterTypes());
                }
            }

            if (method.isAnnotationPresent(SagaTransactionFail.class)) {
                SagaTransactionFail runtimeSagaTransactionFail = method
                        .getAnnotation(SagaTransactionFail.class);
                if (runtimeSagaTransactionFail.sagaTransactionName().equals(sagaTransactionName)) {
                    failInvocationContext = new InvocationContext(targetClass, method, method.getParameterTypes());
                }
            }

            if (method.isAnnotationPresent(SagaTransactionRollback.class)) {
                SagaTransactionRollback runtimeSagaTransactionRollback = method
                        .getAnnotation(SagaTransactionRollback.class);
                if (runtimeSagaTransactionRollback.sagaTransactionName().equals(sagaTransactionName)) {
                    rollbackInvocationContext = new InvocationContext(targetClass, method, method.getParameterTypes());
                }
            }
        }

        SagaTransactionEntity sagaTransactionEntity = new SagaTransactionEntity();
        sagaTransactionEntity.setShardRoutingKey(shardRoutingKey);
        sagaTransactionEntity.setSagaTransactionName(sagaTransactionName);
        sagaTransactionEntity.setBizSerialNo(bizSerialNo);
        sagaTransactionEntity.setSagaTransactionClassName(targetClass.getName());
        sagaTransactionEntity.setSagaTransactionClassMethodName(methodSignature.getMethod().getName());
        sagaTransactionEntity.setParamValues(joinPoint.getArgs());
        sagaTransactionEntity.setParamJson(JsonUtil.toJson(paramJsonList));
        sagaTransactionEntity.setParamTypeJson(JsonUtil.toJson(paramTypeList));
        sagaTransactionEntity.setTransactionStatus(SagaTransactionStatus.INIT.getStatus());
        sagaTransactionEntity.setSagaTransactionType(runtimeSagaTransactionProcess.sagaTransactionType().getType());
        sagaTransactionEntity.setSagaSubTransactionEntities(Lists.newArrayList());
        sagaTransactionEntity.setRecover(recover);
        sagaTransactionEntity.setRetryTime(0);// 设置重试次数的初始值
        sagaTransactionEntity.setSuccessInvocationContext(successInvocationContext);
        sagaTransactionEntity.setFailInvocationContext(failInvocationContext);
        sagaTransactionEntity.setRollbackInvocationContext(rollbackInvocationContext);

        SagaTransactionConfig sagaTransactionConfig = new SagaTransactionConfig();
        sagaTransactionConfig.setSagaTransactionType(runtimeSagaTransactionProcess.sagaTransactionType());
        sagaTransactionConfig.setStartCompensateAfterTransactionName(
                runtimeSagaTransactionProcess.startCompensateAfterTransactionName());
        sagaTransactionConfig.setReExecuteExceptionList(runtimeSagaTransactionProcess.compensateExceptions());
        sagaTransactionConfig.setRollbackExceptionList(runtimeSagaTransactionProcess.rollbackExceptions());
        sagaTransactionConfig.setRetryTime(runtimeSagaTransactionProcess.retryTime());
        sagaTransactionConfig.setRetryInterval(runtimeSagaTransactionProcess.retryInterval());

        return SagaTransactionContext.builder().sagaTransactionEntity(sagaTransactionEntity)
                .sagaTransactionConfig(sagaTransactionConfig).build();
    }

    private Object getAnnotationStrFromParam(ProceedingJoinPoint joinPoint, MethodSignature methodSignature,
                                             Class<? extends Annotation> annotationClass) throws IllegalAccessException {
        if (joinPoint.getArgs() != null) {
            Parameter[] parameters = methodSignature.getMethod().getParameters();
            for (int i = 0; i < joinPoint.getArgs().length; i++) {
                Object arg = joinPoint.getArgs()[i];
                Parameter parameter = parameters[i];
                // 先从方法参数获取
                if (parameter.isAnnotationPresent(annotationClass)) {
                    return arg;
                }
                // 再从方法参数的属性中获取
                if (arg != null) {
                    Class currentClass = arg.getClass();
                    while (currentClass!=null) {
                        Field[] fields = currentClass.getDeclaredFields();
                        for (Field field : fields) {
                            if (field.isAnnotationPresent(annotationClass)) {
                                field.setAccessible(true);
                                return field.get(arg);
                            }
                        }
                        currentClass = currentClass.getSuperclass();
                    }
                }
            }
        }
        return null;
    }
}
