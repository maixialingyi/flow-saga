package com.flow.saga.aspect.Manager;

import com.flow.saga.entity.SagaTransactionContext;
import com.flow.saga.entity.SagaTransactionContextHolder;
import com.flow.saga.entity.SagaTransactionEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SagaTransactionManager extends BaseSagaTransactionManager {

    public void begin(SagaTransactionContext context) {
        //设置上下文
        SagaTransactionContextHolder.putSagaTransactionContext(context);

        SagaTransactionEntity sagaTransactionEntity = context.getSagaTransactionEntity();

        context.addLayerCount();
        if (context.getLayerCount() > 1) {
            log.debug("[SagaSubTransactionProcess]流程{}已经存在顶层saga事务，加入当前事务, 流程类型{}, 业务流水号:{}",
                    sagaTransactionEntity.getSagaTransactionName(), sagaTransactionEntity.getSagaTransactionType(),
                    sagaTransactionEntity.getBizSerialNo());
            return;
        }
        //异常打日志，不影响业务流程
        super.saveSagaTransaction(sagaTransactionEntity);

        // 调用被代理类的方法执行流程
        log.debug("[SagaSubTransactionProcess]流程{}开始, 流程类型{}, 业务流水号:{}",
                sagaTransactionEntity.getSagaTransactionName(), sagaTransactionEntity.getSagaTransactionType(),
                sagaTransactionEntity.getBizSerialNo());
    }

    public void release(SagaTransactionContext context) {
        if (context == null) {
            return;
        }

        context.reduceLayerCount();

        // 如果嵌套层级已经为空，就释放SagaTransactionContextHolder资源
        if (context.getLayerCount() <= 0) {
            SagaTransactionContextHolder.clearSagaTransactionContext();
        }

    }

}
