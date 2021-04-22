package com.flow.saga.entity;

import com.flow.saga.annotation.SagaMainTransactionFail;
import com.flow.saga.annotation.SagaMainTransactionRollback;
import com.flow.saga.annotation.SagaMainTransactionSuccess;
import com.flow.saga.utils.JsonUtil;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@ToString(callSuper = true)
public class SagaTransactionEntity {
    protected static final int MAX_ERROR_MSG_LENGTH = 99;
    /**
     * 流程主键
     */
    private long id;
    /**
     * 分库分表路由键
     */
    private Long shardRoutingKey;
    /**
     * 业务流水号
     */
    private String bizSerialNo;
    /**
     * 流程名称
     */
    private String sagaTransactionName;
    /**
     * 流程类名称
     */
    private String sagaTransactionClassName;
    /**
     * 流程类方法名称
     */
    private String sagaTransactionClassMethodName;
    /**
     * 流程状态
     */
    private int transactionStatus;
    /**
     * 参数json字符串，仅仅在抛异常时才会存下来，用来离线恢复
     */
    private String paramJson;
    /**
     * 参数类型的json字符串，仅仅在抛异常时才会存下来，用来离线恢复
     */
    private String paramTypeJson;
    /**
     * 当前重试次数，这个次数是针对整个saga flow的
     */
    private int retryTime = 0;
    /**
     * 流程处理的类型
     */
    private int sagaTransactionType;
    /**
     * 是否离线恢复
     */
    private boolean recover = false;
    private String errorMsg = "";
    private Long createTime;
    private Long updateTime;
    private int version = 0;

    /**
     * 运行时参数信息，不会持久化到Saga log中
     */
    private Class<?>[] paramTypes;
    private Object[] paramValues;
    private InvocationContext successInvocationContext;
    private InvocationContext failInvocationContext;
    private InvocationContext rollbackInvocationContext;
    /**
     * 子事务列表
     */
    private List<SagaSubTransactionEntity> sagaSubTransactionEntities = Lists.newArrayList();

    public static boolean containsExceptionType(Class<? extends Exception>[] exceptionTypes, Exception e) {
        for (Class<? extends Exception> type : exceptionTypes) {
            if (type.isInstance(e) || type.isInstance(ExceptionUtils.getRootCause(e))) {
                return true;
            }
        }
        return false;

    }

    private static boolean containsSubTransaction(List<String> doneSubTransactionNames,
                                                  String startCompensateAfterTransactionName) {
        return doneSubTransactionNames.contains(startCompensateAfterTransactionName);
    }

    public boolean isFinish() {
        return this.transactionStatus == SagaTransactionStatus.SUCCESS.getStatus()
                || this.transactionStatus == SagaTransactionStatus.ROBACK_SUCCESS.getStatus();
    }

    public boolean isRollbackFail() {
        return this.transactionStatus == SagaTransactionStatus.ROBACK_FAIL.getStatus();
    }

    public void success() {
        this.transactionStatus = SagaTransactionStatus.SUCCESS.getStatus();
    }

    public void rollbackSuccess() {
        this.transactionStatus = SagaTransactionStatus.ROBACK_SUCCESS.getStatus();

    }

    public void rollbackSuccess(String errorMsg) {
        errorMsg = errorMsg != null && errorMsg.length() > MAX_ERROR_MSG_LENGTH
                ? errorMsg.substring(0, MAX_ERROR_MSG_LENGTH)
                : errorMsg;
        this.setErrorMsg(errorMsg);
        this.transactionStatus = SagaTransactionStatus.ROBACK_SUCCESS.getStatus();
    }

    public void rollbackFail() {
        // 失败后需要保存断点信息，这里需要把断点处参数信息保存下来
        List<String> paramValuesStrList = Arrays.stream(this.paramValues).map(JsonUtil::toJson).collect(Collectors.toList());
        this.paramJson = JsonUtil.toJson(paramValuesStrList);
        this.transactionStatus = SagaTransactionStatus.ROBACK_FAIL.getStatus();

    }

    public void rollbackFail(String errorMsg) {
        // 失败后需要保存断点信息，这里需要把断点处参数信息保存下来
        List<String> paramValuesStrList = Arrays.stream(this.paramValues).map(JsonUtil::toJson)
                .collect(Collectors.toList());
        this.paramJson = JsonUtil.toJson(paramValuesStrList);
        errorMsg = errorMsg != null && errorMsg.length() > MAX_ERROR_MSG_LENGTH
                ? errorMsg.substring(0, MAX_ERROR_MSG_LENGTH)
                : errorMsg;
        this.setErrorMsg(errorMsg);
        this.transactionStatus = SagaTransactionStatus.ROBACK_FAIL.getStatus();
    }

    public void fail() {
        // 失败后需要保存断点信息，这里需要把断点处参数信息保存下来
        List<String> paramValuesStrList = Arrays.stream(this.paramValues).map(JsonUtil::toJson)
                .collect(Collectors.toList());
        this.paramJson = JsonUtil.toJson(paramValuesStrList);
        this.transactionStatus = SagaTransactionStatus.FAIL.getStatus();
    }

    public void fail(String errorMsg) {
        // 失败后需要保存断点信息，这里需要把断点处参数信息保存下来
        List<String> paramValuesStrList = Arrays.stream(this.paramValues).map(JsonUtil::toJson)
                .collect(Collectors.toList());
        this.paramJson = JsonUtil.toJson(paramValuesStrList);
        errorMsg = errorMsg != null && errorMsg.length() > MAX_ERROR_MSG_LENGTH
                ? errorMsg.substring(0, MAX_ERROR_MSG_LENGTH)
                : errorMsg;
        this.setErrorMsg(errorMsg);
        this.transactionStatus = SagaTransactionStatus.FAIL.getStatus();
    }

    public void wrapErrorInfo(String errorMsg) {
        errorMsg = errorMsg != null && errorMsg.length() > MAX_ERROR_MSG_LENGTH
                ? errorMsg.substring(0, MAX_ERROR_MSG_LENGTH)
                : errorMsg;
        this.setErrorMsg(errorMsg);
    }

    public void successClearParam() {
        this.paramJson = "";
        this.paramTypeJson = "";
    }

    /**
     * 是否需要回滚
     */
    public boolean needRollback(Class<? extends Exception>[] exceptionTypes, Exception e) {

        if (this.getSagaTransactionType() == SagaTransactionTypeEnum.ROLLBACK.getType()) {
            return true;
        }

        if (this.getSagaTransactionType() == SagaTransactionTypeEnum.CONFIG_BY_EXCEPTION.getType()
                && containsExceptionType(exceptionTypes, e)) {
            return true;
        }

        return false;
    }

    /**
     * 是否需要重试判断
     */
    public boolean needRetryCheckAndAddRetryTime(Class<? extends Exception>[] exceptionTypes,
                                                 Exception e, Integer retryMaxTime) {
        if (isCompensateExceptionType(exceptionTypes, e)
                && !exceedMaxRetryTimes(retryMaxTime)) {
            this.retryTime++;
            return true;
        }

        return false;
    }

    /**
     * 是否命中重试策略
     */
    public boolean isCompensateExceptionType(Class<? extends Exception>[] exceptionTypes,Exception e) {
        if (this.getSagaTransactionType() == SagaTransactionTypeEnum.RE_EXECUTE.getType()) {
            return true;
        }
        if (this.getSagaTransactionType() == SagaTransactionTypeEnum.CONFIG_BY_EXCEPTION.getType()
                && containsExceptionType(exceptionTypes, e)) {
            return true;
        }
        return false;
    }

    private boolean exceedMaxRetryTimes(Integer retryMaxTime) {
        return this.retryTime >= retryMaxTime;
    }

    public Object[] getAndConstructParamValues() throws ClassNotFoundException {
        if (this.paramValues != null) {
            return this.paramValues;
        }
        List<String> paramTypeNames = JsonUtil.fromJson(this.getParamTypeJson(), new TypeReference<List<String>>() {
        });
        List<String> paramValues = JsonUtil.fromJson(this.getParamJson(), new TypeReference<List<String>>() {
        });

        List<Object> objectList = Lists.newArrayList();
        for (int i = 0; i < paramTypeNames.size(); i++) {
            objectList.add(JsonUtil.fromJson(paramValues.get(i),
                    Thread.currentThread().getContextClassLoader().loadClass(paramTypeNames.get(i))));
        }
        this.paramValues = objectList.toArray();

        return objectList.toArray();
    }

    private Class<?>[] getAndConstructParamTypes() throws ClassNotFoundException {
        if (this.paramTypes != null) {
            return this.paramTypes;
        }
        List<String> paramTypeNames = JsonUtil.fromJson(this.getParamTypeJson(), new TypeReference<List<String>>() {
        });
        Class<?>[] classTypes = new Class[paramTypeNames.size()];
        for (int i = 0; i < paramTypeNames.size(); i++) {
            Class<?> classType = Thread.currentThread().getContextClassLoader().loadClass(paramTypeNames.get(i));
            classTypes[i] = classType;
        }
        paramTypes = classTypes;
        return classTypes;
    }

    public InvocationContext getAndConstructSuccessInvocationContext() throws ClassNotFoundException {
        if (successInvocationContext != null) {
            return successInvocationContext;
        }

        Class<?> targetClass = Thread.currentThread().getContextClassLoader().loadClass(sagaTransactionClassName);
        for (Method method : targetClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(SagaMainTransactionSuccess.class)) {
                SagaMainTransactionSuccess sagaMainTransactionSuccess = method.getAnnotation(SagaMainTransactionSuccess.class);
                if (sagaMainTransactionSuccess.sagaTransactionName().equals(sagaTransactionName)) {
                    successInvocationContext = new InvocationContext(targetClass, method,this.getAndConstructParamTypes());
                }
            }
        }
        return successInvocationContext;
    }

    public InvocationContext getAndConstructFailInvocationContext() throws ClassNotFoundException {
        if (failInvocationContext != null) {
            return failInvocationContext;
        }
        Class<?> targetClass = Thread.currentThread().getContextClassLoader().loadClass(sagaTransactionClassName);
        for (Method method : targetClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(SagaMainTransactionFail.class)) {
                SagaMainTransactionFail runtimeSagaMainTransactionFail = method
                        .getAnnotation(SagaMainTransactionFail.class);
                if (runtimeSagaMainTransactionFail.sagaTransactionName().equals(sagaTransactionName)) {
                    failInvocationContext = new InvocationContext(targetClass, method,
                            this.getAndConstructParamTypes());
                }
            }
        }
        return failInvocationContext;
    }

    public InvocationContext getAndConstructRollbackInvocationContext() throws ClassNotFoundException {
        if (rollbackInvocationContext != null) {
            return rollbackInvocationContext;
        }
        Class<?> targetClass = Thread.currentThread().getContextClassLoader().loadClass(sagaTransactionClassName);
        for (Method method : targetClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(SagaMainTransactionRollback.class)) {
                SagaMainTransactionRollback runtimeSagaMainTransactionRollback = method
                        .getAnnotation(SagaMainTransactionRollback.class);
                if (runtimeSagaMainTransactionRollback.sagaTransactionName().equals(sagaTransactionName)) {
                    rollbackInvocationContext = new InvocationContext(targetClass, method,
                            this.getAndConstructParamTypes());
                }
            }
        }
        return rollbackInvocationContext;
    }

    public void addSubTransaction(SagaSubTransactionEntity sagaSubTransactionEntity) {
        if (CollectionUtils.isEmpty(this.sagaSubTransactionEntities)) {
            this.sagaSubTransactionEntities = Lists.newArrayList();
        }
        this.sagaSubTransactionEntities.add(sagaSubTransactionEntity);
    }

    public void fillShardRoutingKeyIfAbsent() {
        if (this.shardRoutingKey == 0) {
            this.shardRoutingKey = this.id;
        }
    }

}
