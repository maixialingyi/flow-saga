package com.flow.saga.service;

import com.flow.saga.annotation.*;
import com.flow.saga.entity.SagaTransactionTypeEnum;
import com.flow.saga.exception.SagaFlowSystemException;
import com.flow.saga.exception.SagaTransactionReExecuteException;
import com.flow.saga.exception.SagaTransactionRollbackException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TestNormalSubFlowSagaServiceImpl {

    @SagaSubTransactionProcess(sagaSubTransactionName = "test-sub-flow-nomal-1")
    public FlowSagaServiceResponseDTO testSubFlowNomal_1(FlowSagaServiceRequestDTO requestDTO){
        log.info("子事务test-sub-flow-nomal-执行成功");
        return null;
    }
    @SagaSubTransactionSuccess(sagaSubTransactionName = "test-sub-flow-nomal-1")
    public FlowSagaServiceResponseDTO testSubFlowNomal_1_Success(FlowSagaServiceRequestDTO requestDTO){
        log.info("子事务test-sub-flow-nomal-1执行成功后__回调方法");
        return null;
    }
}
