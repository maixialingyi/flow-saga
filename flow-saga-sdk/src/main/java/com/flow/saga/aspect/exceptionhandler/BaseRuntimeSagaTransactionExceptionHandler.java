package com.flow.saga.aspect.exceptionhandler;

import com.flow.saga.entity.InvocationContext;
import com.flow.saga.entity.SagaSubTransactionEntity;
import com.flow.saga.entity.SagaTransactionEntity;
import com.flow.saga.entity.SagaTransactionStatus;
import com.flow.saga.utils.BeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BaseRuntimeSagaTransactionExceptionHandler {

    //回滚处理
    public void handleRollback(SagaTransactionEntity sagaTransactionEntity, Exception e) {
        List<SagaSubTransactionEntity> sagaSubTransactionEntities = sagaTransactionEntity
                .getSagaSubTransactionEntities();
        // 反转事务顺序
        Collections.reverse(sagaSubTransactionEntities);

        // 回滚失败或执行成功 -> 回滚
        sagaSubTransactionEntities.stream().filter(sagaSubTransactionEntity -> sagaSubTransactionEntity.isSuccess()
                || sagaSubTransactionEntity.isRollbackFail()).forEach(sagaSubTransactionEntity -> {

                    try {
                        InvocationContext rollbackInvocationContext = sagaSubTransactionEntity.getAndConstructRollbackInvocationContext();
                        if (rollbackInvocationContext == null) {
                            return;
                        }
                        Object service = BeanUtil.getBean(rollbackInvocationContext.getTargetClass());
                        Object[] params = sagaSubTransactionEntity.getAndConstructParamValues();
                        Object[] paramsWithException;
                        if (params == null || params.length == 0) {
                            paramsWithException = new Object[] { e };
                        } else {
                            paramsWithException = ArrayUtils.add(params, e);
                        }
                        ReflectionUtils.invokeMethod(rollbackInvocationContext.getMethod(), service,paramsWithException);
                        sagaSubTransactionEntity.rollbackSuccess();
                    } catch (Exception ee) {
                        sagaSubTransactionEntity.rollbackFail();
                        log.error("[SagaSubTransactionProcess]子流程{}, 处理子事务回滚，反射执行子事务回滚方法失败, 业务流水号:{}",
                                sagaSubTransactionEntity.getSubTransactionName(),
                                sagaSubTransactionEntity.getBizSerialNo(), ee);
                    }

                });

        // 所有子事务回滚成功，在执行顶层事务回滚方法。
        // 全部执行成功则整个saga flow回滚成功，否则回滚失败
        if (sagaSubTransactionEntities.stream()
                .filter(sagaSubTransactionEntity -> sagaSubTransactionEntity
                        .getTransactionStatus() == SagaTransactionStatus.ROBACK_FAIL.getStatus())
                .collect(Collectors.toList()).isEmpty()) {
            // 执行顶层事务的回滚方法
            try {
                InvocationContext rollbackInvocationContext = sagaTransactionEntity.getAndConstructRollbackInvocationContext();
                if (rollbackInvocationContext != null) {
                    Object service = BeanUtil.getBean(rollbackInvocationContext.getTargetClass());
                    Object[] params = sagaTransactionEntity.getAndConstructParamValues();
                    Object[] paramsWithException;
                    if (params == null || params.length == 0) {
                        paramsWithException = new Object[] { e };
                    } else {
                        paramsWithException = ArrayUtils.add(params, e);
                    }
                    ReflectionUtils.invokeMethod(rollbackInvocationContext.getMethod(), service, paramsWithException);
                    sagaTransactionEntity.rollbackSuccess();
                }
                sagaTransactionEntity.rollbackSuccess();
            } catch (Exception e1) {
                sagaTransactionEntity.rollbackFail();
                log.error("[SagaSubTransactionProcess]流程{}, 处理事务回滚，反射执行事务回滚方法失败, 业务流水号:{}",
                        sagaTransactionEntity.getSagaTransactionName(), sagaTransactionEntity.getBizSerialNo(), e1);
            }
        } else {
            sagaTransactionEntity.rollbackFail();
        }
    }
}
