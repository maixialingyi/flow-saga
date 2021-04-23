package com.flow.saga.service;

import com.flow.saga.annotation.*;
import com.flow.saga.entity.SagaTransactionTypeEnum;
import com.flow.saga.exception.SagaFlowSystemException;
import com.flow.saga.exception.SagaTransactionReExecuteException;
import com.flow.saga.exception.SagaTransactionRollbackException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 事务类型为 : 回滚或者重新执行(通过子事务异常配置)
 * 子事务1成功执行
 * 子事务2异常 -> 重试,回滚.先重试后回滚
 * 子事务2正常 -> 事务传播
 *
 * 方法入参：引用类型，基本类型，无入参      还未测试
 * 方法出参：引用类型，基本类型，void       还未测试
 *
 * 备注：嵌套注解不能在同一对象中，嵌套的掉用不到代理对象
 */
@Slf4j
@Service
public class TestExMainFlowSagaServiceImpl {

    @Autowired
    private TestExSubFlowSagaServiceImpl testExSubFlowSagaServiceImpl;

    /** -----------------------主事务--------------------*/
    @SagaMainTransactionProcess(sagaTransactionName = "test-main-flow-ex",
            sagaTransactionType = SagaTransactionTypeEnum.CONFIG_BY_EXCEPTION,
            retryTime = 3,
            retryInterval = 1000)
    public FlowSagaServiceResponseDTO testTransactionMainFlowEx(FlowSagaServiceRequestDTO requestDTO){
        testExSubFlowSagaServiceImpl.testSubFlowEx_1(requestDTO);
        testExSubFlowSagaServiceImpl.testSubFlowEx_2(requestDTO);
        log.info("主事务test-main-flow-ex执行成功");
        return null;
    }

    @SagaMainTransactionSuccess(sagaTransactionName = "test-main-flow-ex")
    public FlowSagaServiceResponseDTO testTransactionMainFlowExSuccess(FlowSagaServiceRequestDTO requestDTO){
        log.info("主事务test-main-flow-ex执行成功后__回调方法");
        return null;
    }
    @SagaMainTransactionRollback(sagaTransactionName = "test-main-flow-ex")
    public FlowSagaServiceResponseDTO testTransactionMainFlowExRollback(FlowSagaServiceRequestDTO requestDTO,Exception e){
        log.info("主事务test-main-flow-ex执行回滚__回调方法");
        return null;
    }
    @SagaMainTransactionFail(sagaTransactionName = "test-main-flow-ex")
    public FlowSagaServiceResponseDTO testTransactionMainFlowExFail(FlowSagaServiceRequestDTO requestDTO,Exception e){
        log.info("主事务test-main-flow-ex执行失败后__回调方法");
        return null;
    }
}
