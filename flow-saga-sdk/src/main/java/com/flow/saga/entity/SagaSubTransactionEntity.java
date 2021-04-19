package com.flow.saga.entity;
import com.flow.saga.annotation.SagaSubTransactionFail;
import com.flow.saga.annotation.SagaSubTransactionRollback;
import com.flow.saga.annotation.SagaSubTransactionSuccess;
import com.flow.saga.utils.JsonUtil;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.codehaus.jackson.type.TypeReference;

import java.lang.reflect.Method;
import java.util.List;

@Getter
@Setter
@ToString(callSuper = true)
public class SagaSubTransactionEntity {
    protected static final int MAX_ERROR_MSG_LENGTH = 99;
    /**
     * 业务流程步骤的id
     */
    private long id;
    /**
     * 分库分表路由键
     */
    private Long shardRoutingKey;
    /**
     * 顶层SagaFlowId
     */
    private long sagaTransactionId;
    /**
     * 业务步骤名称
     */
    private String subTransactionName;
    /**
     * 子事务类名
     */
    private String subTransactionClassName;
    /**
     * 子事务方法名
     */
    private String subTransactionClassMethodName;
    /**
     * 业务流水号
     */
    private String bizSerialNo;
    /**
     * 该业务步骤的执行状态
     */
    private int transactionStatus;
    /**
     * 参数json字符串，仅仅在抛异常时才会存下来，用来rollback或者补偿
     */
    private String paramValueJson;
    /**
     * 参数类型的json字符串，仅仅在抛异常时才会存下来，用来rollback或者补偿
     */
    private String paramTypeJson;
    /**
     * 返回值类型json字符串
     */
    private String returnTypeJson = "";
    /**
     * 返回值json字符串
     */
    private String returnValueJson = "";
    private String errorMsg = "";
    private Integer version = 0;
    private Long createTime;
    private Long updateTime;
    /**
     * 运行时参数信息，不会持久化到Saga log中
     */
    private Class<?>[] paramTypes;
    private Object[] paramValues;
    private InvocationContext successInvocationContext;
    private InvocationContext failInvocationContext;
    private InvocationContext rollbackInvocationContext;
    private Class<? extends Exception>[] compensateExceptions;
    private Class<? extends Exception>[] rollbackExceptions;

    public void clearParam() {
        this.paramValueJson = "";
        this.paramTypeJson = "";
    }

    public void clearReturn() {
        this.returnTypeJson = "";
        this.returnValueJson = "";
    }

    public void success() {
        this.transactionStatus = SagaTransactionStatus.SUCCESS.getStatus();
    }

    public void fail(String errorMsg) {
        errorMsg = errorMsg != null && errorMsg.length() > MAX_ERROR_MSG_LENGTH
                ? errorMsg.substring(0, MAX_ERROR_MSG_LENGTH)
                : errorMsg;
        this.setErrorMsg(errorMsg);
        this.transactionStatus = SagaTransactionStatus.FAIL.getStatus();
    }

    public void initStatus() {
        this.transactionStatus = SagaTransactionStatus.INIT.getStatus();
    }

    public void rollbackFail() {
        this.transactionStatus = SagaTransactionStatus.ROBACK_FAIL.getStatus();

    }

    public void rollbackFail(String errorMsg) {
        errorMsg = errorMsg != null && errorMsg.length() > MAX_ERROR_MSG_LENGTH
                ? errorMsg.substring(0, MAX_ERROR_MSG_LENGTH)
                : errorMsg;
        this.setErrorMsg(errorMsg);
        this.transactionStatus = SagaTransactionStatus.ROBACK_FAIL.getStatus();
    }

    public void wrapErrorInfo(String errorMsg) {
        errorMsg = errorMsg != null && errorMsg.length() > MAX_ERROR_MSG_LENGTH
                ? errorMsg.substring(0, MAX_ERROR_MSG_LENGTH)
                : errorMsg;
        this.setErrorMsg(errorMsg);
    }

    public void rollbackSuccess() {
        this.transactionStatus = SagaTransactionStatus.ROBACK_SUCCESS.getStatus();

    }

    public void rollbackSuccess(String errorMsg) {
        errorMsg = errorMsg != null && errorMsg.length() > MAX_ERROR_MSG_LENGTH
                ? errorMsg.substring(0, MAX_ERROR_MSG_LENGTH)
                : errorMsg;
        this.setErrorMsg(errorMsg);
        this.transactionStatus = SagaTransactionStatus.ROBACK_FAIL.getStatus();
    }

    public boolean isSuccess() {
        return this.transactionStatus == SagaTransactionStatus.SUCCESS.getStatus();
    }

    public boolean isRollbackFail() {
        return this.transactionStatus == SagaTransactionStatus.ROBACK_FAIL.getStatus();
    }

    public boolean isFail() {
        return this.transactionStatus == SagaTransactionStatus.FAIL.getStatus();
    }

    public Object[] getAndConstructParamValues() throws ClassNotFoundException {
        if (this.paramValues != null) {
            return this.paramValues;
        }
        List<String> paramTypeNames = JsonUtil.fromJson(this.getParamTypeJson(), new TypeReference<List<String>>() {
        });
        List<String> paramValues = JsonUtil.fromJson(this.getParamValueJson(), new TypeReference<List<String>>() {
        });
        List<Object> objectList = Lists.newArrayList();
        Class<?>[] classTypes = new Class[paramTypeNames.size()];
        for (int i = 0; i < paramTypeNames.size(); i++) {
            Class<?> classType = Thread.currentThread().getContextClassLoader().loadClass(paramTypeNames.get(i));
            classTypes[i] = classType;
            objectList.add(JsonUtil.fromJson(paramValues.get(i), classType));
        }
        this.paramValues = objectList.toArray();
        this.paramTypes = classTypes;
        return objectList.toArray();
    }

    public Object getAndConstructReturnValue() throws ClassNotFoundException {
        // void返回类型特殊处理
        if (Strings.isNullOrEmpty(returnTypeJson) || "void".equals(returnTypeJson)
                || returnTypeJson.endsWith(JsonUtil.toJson(void.class))) {
            return null;
        }
        return JsonUtil.fromJson(returnValueJson,
                Thread.currentThread().getContextClassLoader().loadClass(returnTypeJson));
    }

    public Class<?>[] getAndConstructParamTypes() throws ClassNotFoundException {
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

        Class<?> targetClass = Thread.currentThread().getContextClassLoader().loadClass(subTransactionClassName);
        for (Method method : targetClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(SagaSubTransactionSuccess.class)) {
                SagaSubTransactionSuccess runtimeSagaSubTransactionSuccess = method
                        .getAnnotation(SagaSubTransactionSuccess.class);
                if (runtimeSagaSubTransactionSuccess.sagaSubTransactionName().equals(subTransactionName)) {
                    successInvocationContext = new InvocationContext(targetClass, method,
                            this.getAndConstructParamTypes());
                }

            }
        }
        return successInvocationContext;
    }

    public InvocationContext getAndConstructFailInvocationContext() throws ClassNotFoundException {
        if (failInvocationContext != null) {
            return failInvocationContext;
        }

        Class<?> targetClass = Thread.currentThread().getContextClassLoader().loadClass(subTransactionClassName);
        for (Method method : targetClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(SagaSubTransactionFail.class)) {
                SagaSubTransactionFail runtimeSagaSubTransactionFail = method
                        .getAnnotation(SagaSubTransactionFail.class);
                if (runtimeSagaSubTransactionFail.sagaSubTransactionName().equals(subTransactionName)) {
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

        Class<?> targetClass = Thread.currentThread().getContextClassLoader().loadClass(subTransactionClassName);
        for (Method method : targetClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(SagaSubTransactionRollback.class)) {
                SagaSubTransactionRollback runtimeSagaSubTransactionRollback = method
                        .getAnnotation(SagaSubTransactionRollback.class);
                if (runtimeSagaSubTransactionRollback.sagaSubTransactionName().equals(subTransactionName)) {
                    rollbackInvocationContext = new InvocationContext(targetClass, method,
                            this.getAndConstructParamTypes());
                }

            }
        }
        return rollbackInvocationContext;
    }

    public void fillShardRoutingKeyIfAbsent() {
        if (this.shardRoutingKey == 0) {
            this.shardRoutingKey = this.id;
        }
    }
}
