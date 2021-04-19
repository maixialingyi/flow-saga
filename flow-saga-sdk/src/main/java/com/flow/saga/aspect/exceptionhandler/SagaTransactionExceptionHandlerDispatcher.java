package com.flow.saga.aspect.exceptionhandler;

import com.flow.saga.entity.SagaTransactionContext;
import com.flow.saga.entity.SagaTransactionEntity;
import com.flow.saga.entity.SagaTransactionTypeEnum;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 异常处理器调度类
 */
@Slf4j
@Component
public class SagaTransactionExceptionHandlerDispatcher {

    @Autowired
    private List<SagaTransactionExceptionHandler> runtimeSagaFlowExceptionHandlerList;

    private Map<SagaTransactionTypeEnum, SagaTransactionExceptionHandler> runtimeSagaFlowExceptionHandlerMap = Maps.newHashMap();

    @PostConstruct
    private void initRuntimeSagaFlowExceptionHandlerMap() {
        runtimeSagaFlowExceptionHandlerMap = runtimeSagaFlowExceptionHandlerList.stream()
                .collect(Collectors.toMap(SagaTransactionExceptionHandler::getSagaTransactionType, handler -> handler));
    }

    public void handleException(SagaTransactionContext context, Exception e) {

        SagaTransactionExceptionHandler sagaTransactionExceptionHandler = runtimeSagaFlowExceptionHandlerMap
                .get(context.getSagaTransactionConfig().getSagaTransactionType());

        SagaTransactionEntity sagaTransactionEntity = context.getSagaTransactionEntity();
        sagaTransactionEntity.wrapErrorInfo(e.getMessage());

        sagaTransactionExceptionHandler.handleException(context, e);
    }
}
