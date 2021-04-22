package com.flow.saga.service;

import com.flow.saga.annotation.*;
import com.flow.saga.entity.SagaTransactionTypeEnum;
import com.flow.saga.exception.SagaFlowSystemException;
import com.flow.saga.exception.SagaTransactionReExecuteException;
import com.flow.saga.exception.SagaTransactionRollbackException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 事务类型为 : 回滚或者重新执行(通过子事务异常配置)
 * 子事务1成功执行
 * 子事务2异常 -> 重试,回滚.先重试后回滚
 * 子事务2正常 -> 事务传播
 *
 * 方法入参：引用类型，基本类型，无入参      还未测试
 * 方法出参：引用类型，基本类型，void       还未测试
 */
@Slf4j
@Service
public class TestNormalFlowSagaServiceImpl {

    /** -----------------------主事务--------------------*/
    @SagaMainTransactionProcess(sagaTransactionName = "test-main-flow-ex",
            sagaTransactionType = SagaTransactionTypeEnum.CONFIG_BY_EXCEPTION,
            retryTime = 3,
            retryInterval = 1000)
    public FlowSagaServiceResponseDTO testTransactionMainFlowEx(FlowSagaServiceRequestDTO requestDTO){
        return null;
    }

    @SagaMainTransactionSuccess(sagaTransactionName = "test-main-flow-ex")
    public FlowSagaServiceResponseDTO testTransactionMainFlowExSuccess(FlowSagaServiceRequestDTO requestDTO){
        return null;
    }
    @SagaMainTransactionRollback(sagaTransactionName = "test-main-flow-ex")
    public FlowSagaServiceResponseDTO testTransactionMainFlowExRollback(FlowSagaServiceRequestDTO requestDTO,Exception e){
        return null;
    }
    @SagaMainTransactionFail(sagaTransactionName = "test-main-flow-ex")
    public FlowSagaServiceResponseDTO testTransactionMainFlowExFail(FlowSagaServiceRequestDTO requestDTO,Exception e){
        return null;
    }

    /** 子事务1*/
    @SagaSubTransactionProcess(sagaSubTransactionName = "test-sub-flow-ex-1")
    public FlowSagaServiceResponseDTO testSubFlowEx_1(FlowSagaServiceRequestDTO requestDTO){
        log.info("mylog --- 子事务__1__执行成功");
        return null;
    }

    @SagaSubTransactionSuccess(sagaSubTransactionName = "test-sub-flow-ex-1")
    public FlowSagaServiceResponseDTO testSubFlowEx_1_Success(FlowSagaServiceRequestDTO requestDTO,Exception e){
        log.info("mylog --- 子事务__1__执行成功后__回调方法");
        return null;
    }

    @SagaSubTransactionRollback(sagaSubTransactionName = "test-sub-flow-ex-1")
    public FlowSagaServiceResponseDTO testSubFlowEx_1_Rollback(FlowSagaServiceRequestDTO requestDTO,Exception e){
        log.info("mylog --- 子事务__1__执行回滚__回调方法");
        return null;
    }

    @SagaSubTransactionFail(sagaSubTransactionName = "test-sub-flow-ex-1")
    public FlowSagaServiceResponseDTO testSubFlowEx_1_Fail(FlowSagaServiceRequestDTO requestDTO,Exception e){
        log.info("mylog --- 子事务__1__执行失败后__回调方法");
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
        log.info("mylog --- 子事务__2__执行成功");
        return null;
    }

    @SagaSubTransactionSuccess(sagaSubTransactionName = "test-sub-flow-ex-2")
    public FlowSagaServiceResponseDTO testSubFlowEx_2_Success(FlowSagaServiceRequestDTO requestDTO,Exception e){
        log.info("mylog --- 子事务__2__执行成功后__回调方法");
        return null;
    }

    @SagaSubTransactionRollback(sagaSubTransactionName = "test-sub-flow-ex-2")
    public FlowSagaServiceResponseDTO testSubFlowEx_2_Rollback(FlowSagaServiceRequestDTO requestDTO,Exception e){
        log.info("mylog --- 子事务__2__执行回滚__回调方法");
        return null;
    }

    @SagaSubTransactionFail(sagaSubTransactionName = "test-sub-flow-ex-2")
    public FlowSagaServiceResponseDTO testSubFlowEx_2_Fail(FlowSagaServiceRequestDTO requestDTO,Exception e){
        log.info("mylog --- 子事务__2__执行失败后__回调方法");
        return null;
    }

    /** 事务嵌套 */
    @SagaMainTransactionProcess(sagaTransactionName = "test-main-flow-nomal",
            sagaTransactionType = SagaTransactionTypeEnum.CONFIG_BY_EXCEPTION,
            retryTime = 3,
            retryInterval = 1000)
    public FlowSagaServiceResponseDTO testMainFlowNomal(FlowSagaServiceRequestDTO requestDTO){
        this.testSubFlowNomal_1(requestDTO);
        return null;
    }
    @SagaMainTransactionSuccess(sagaTransactionName = "test-main-flow-nomal")
    public FlowSagaServiceResponseDTO testMainFlowNomalSuccess(FlowSagaServiceRequestDTO requestDTO){
        log.info("mylog --- 主事务test-main-flow-nomal执行成功后__回调方法");
        return null;
    }

    @SagaSubTransactionProcess(sagaSubTransactionName = "test-sub-flow-nomal-1")
    public FlowSagaServiceResponseDTO testSubFlowNomal_1(FlowSagaServiceRequestDTO requestDTO){
        log.info("mylog --- 子事务test-sub-flow-nomal-执行成功");
        return null;
    }
    @SagaSubTransactionSuccess(sagaSubTransactionName = "test-sub-flow-nomal-1")
    public FlowSagaServiceResponseDTO testSubFlowNomal_1_Success(FlowSagaServiceRequestDTO requestDTO){
        log.info("mylog --- 子事务test-sub-flow-nomal-1执行成功后__回调方法");
        return null;
    }
}
