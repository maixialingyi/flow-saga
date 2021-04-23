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
public class TestExSubFlowSagaServiceImpl {

    /** 子事务1*/
    @SagaSubTransactionProcess(sagaSubTransactionName = "test-sub-flow-ex-1")
    public FlowSagaServiceResponseDTO testSubFlowEx_1(FlowSagaServiceRequestDTO requestDTO){
        log.info("子事务test-sub-flow-ex-1执行成功");
        return null;
    }

    @SagaSubTransactionSuccess(sagaSubTransactionName = "test-sub-flow-ex-1")
    public FlowSagaServiceResponseDTO testSubFlowEx_1_Success(FlowSagaServiceRequestDTO requestDTO,Exception e){
        log.info("子事务test-sub-flow-ex-1执行成功后__回调方法");
        return null;
    }

    @SagaSubTransactionRollback(sagaSubTransactionName = "test-sub-flow-ex-1")
    public FlowSagaServiceResponseDTO testSubFlowEx_1_Rollback(FlowSagaServiceRequestDTO requestDTO,Exception e){
        log.info("子事务test-sub-flow-ex-1执行回滚__回调方法");
        return null;
    }

    @SagaSubTransactionFail(sagaSubTransactionName = "test-sub-flow-ex-1")
    public FlowSagaServiceResponseDTO testSubFlowEx_1_Fail(FlowSagaServiceRequestDTO requestDTO,Exception e){
        log.info("子事务test-sub-flow-ex-1执行失败后__回调方法");
        return null;
    }

    /** 子事务2*/
    @SagaSubTransactionProcess(sagaSubTransactionName = "test-sub-flow-ex-2",
                               reExecuteExceptions = {SagaTransactionReExecuteException.class,
                                                      SagaFlowSystemException.class},
                               rollbackExceptions = {SagaTransactionRollbackException.class,
                                                      SagaFlowSystemException.class})
    public FlowSagaServiceResponseDTO testSubFlowEx_2(FlowSagaServiceRequestDTO requestDTO){
        // 重试, 重试仍失败后 -> 恢复
        if(1==1){ throw new SagaTransactionReExecuteException(); }

        // 回滚, 回滚仍失败后 -> 恢复
        if(1==1){ throw new SagaTransactionRollbackException(); }

        // 先重试仍失败后回滚，回滚仍失败后 -> 恢复
        if(1==1){ throw new SagaFlowSystemException(); }
        log.info("子事务test-sub-flow-ex-2执行成功");
        return null;
    }

    @SagaSubTransactionSuccess(sagaSubTransactionName = "test-sub-flow-ex-2")
    public FlowSagaServiceResponseDTO testSubFlowEx_2_Success(FlowSagaServiceRequestDTO requestDTO,Exception e){
        log.info("子事务test-sub-flow-ex-2执行成功后__回调方法");
        return null;
    }

    @SagaSubTransactionRollback(sagaSubTransactionName = "test-sub-flow-ex-2")
    public FlowSagaServiceResponseDTO testSubFlowEx_2_Rollback(FlowSagaServiceRequestDTO requestDTO,Exception e){
        log.info("子事务test-sub-flow-ex-2执行回滚__回调方法");
        return null;
    }

    @SagaSubTransactionFail(sagaSubTransactionName = "test-sub-flow-ex-2")
    public FlowSagaServiceResponseDTO testSubFlowEx_2_Fail(FlowSagaServiceRequestDTO requestDTO,Exception e){
        log.info("子事务test-sub-flow-ex-2执行失败后__回调方法");
        return null;
    }

}
