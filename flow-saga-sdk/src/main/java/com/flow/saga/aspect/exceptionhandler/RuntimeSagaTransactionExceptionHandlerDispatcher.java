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
 * @author: songzeqi
 * @Date: 2019-07-16 11:30 AM
 */
@Slf4j
@Component
public class RuntimeSagaTransactionExceptionHandlerDispatcher implements RuntimeSagaTransactionExceptionHandler {

    @Autowired
    private List<RuntimeSagaTransactionExceptionHandler> runtimeSagaFlowExceptionHandlerList;

    private Map<SagaTransactionTypeEnum, RuntimeSagaTransactionExceptionHandler> runtimeSagaFlowExceptionHandlerMap = Maps
            .newHashMap();

    @Override
    public void handleException(SagaTransactionContext context, Exception e) {

        RuntimeSagaTransactionExceptionHandler runtimeSagaTransactionExceptionHandler = runtimeSagaFlowExceptionHandlerMap
                .get(context.getSagaTransactionConfig().getSagaTransactionType());

        SagaTransactionEntity sagaTransactionEntity = context.getSagaTransactionEntity();
        sagaTransactionEntity.wrapErrorInfo(e.getMessage());

        runtimeSagaTransactionExceptionHandler.handleException(context, e);

    }

    @Override
    public SagaTransactionTypeEnum getSagaTransactionType() {
        return null;
    }

    @PostConstruct
    private void initRuntimeSagaFlowExceptionHandlerMap() {
        runtimeSagaFlowExceptionHandlerMap = runtimeSagaFlowExceptionHandlerList.stream()
                .filter(handler -> handler.getSagaTransactionType() != null)
                .collect(Collectors.toMap(RuntimeSagaTransactionExceptionHandler::getSagaTransactionType, handler -> handler));
    }

}
