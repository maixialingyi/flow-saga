package com.flow.saga.service;

import com.flow.saga.annotation.*;
import com.flow.saga.entity.SagaTransactionTypeEnum;
import com.flow.saga.exception.SagaFlowSystemException;
import com.flow.saga.exception.SagaTransactionReExecuteException;
import com.flow.saga.exception.SagaTransactionRollbackException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TestNormalMainFlowSagaServiceImpl {

    @Autowired
    private TestNormalSubFlowSagaServiceImpl testNormalSubFlowSagaServiceImpl;

    /** 成功执行，事务嵌套测试 */
    @SagaMainTransactionProcess(sagaTransactionName = "test-main-flow-nomal",
            sagaTransactionType = SagaTransactionTypeEnum.CONFIG_BY_EXCEPTION,
            retryTime = 3,
            retryInterval = 1000)
    public FlowSagaServiceResponseDTO testMainFlowNomal(FlowSagaServiceRequestDTO requestDTO){
        testNormalSubFlowSagaServiceImpl.testSubFlowNomal_1(requestDTO);
        return null;
    }
    @SagaMainTransactionSuccess(sagaTransactionName = "test-main-flow-nomal")
    public FlowSagaServiceResponseDTO testMainFlowNomalSuccess(FlowSagaServiceRequestDTO requestDTO){
        log.info("主事务test-main-flow-nomal执行成功后__回调方法");
        return null;
    }
}
