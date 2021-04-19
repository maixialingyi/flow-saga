package com.flow.saga.recover;

import com.flow.saga.entity.SagaSubTransactionEntity;
import com.flow.saga.entity.SagaTransactionContext;
import com.flow.saga.entity.SagaTransactionContextHolder;
import com.flow.saga.entity.SagaTransactionEntity;
import com.flow.saga.repository.SagaLogRepository;
import com.flow.saga.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 离线恢复入口
 */
@Slf4j
@Service
public class SagaTransactionRecoverService{

    @Resource
    private SagaLogRepository sagaLogRepository;

    @Autowired
    private ApplicationContext applicationContext;

    public void recover(SagaTransactionEntity sagaTransactionEntity) {

        List<SagaSubTransactionEntity> sagaSubTransactionEntities = sagaLogRepository.querySagaSubTransactionsById(
                sagaTransactionEntity.getId(), sagaTransactionEntity.getShardRoutingKey());
        sagaTransactionEntity.setSagaSubTransactionEntities(sagaSubTransactionEntities);
        // 置为恢复模式
        SagaTransactionContext sagaTransactionContext = SagaTransactionContext.builder()
                .sagaTransactionEntity(sagaTransactionEntity).recover(true).build();
        SagaTransactionContextHolder.putSagaTransactionContext(sagaTransactionContext);

        try {
            String flowClassName = sagaTransactionEntity.getSagaTransactionClassName();
            Class<?> sagaFlowClass = Thread.currentThread().getContextClassLoader().loadClass(flowClassName);
            Object service = applicationContext.getBean(sagaFlowClass);

            String sagaFlowClassMethodNme = sagaTransactionEntity.getSagaTransactionClassMethodName();
            Class<?>[] paramClass = JsonUtil.fromJson(sagaTransactionEntity.getParamTypeJson(),
                    new TypeReference<Class<?>[]>() {
                    });
            // 找到方法
            Method method = ReflectionUtils.findMethod(service.getClass(), sagaFlowClassMethodNme, paramClass);
            // 执行方法
            ReflectionUtils.invokeMethod(method, service, sagaTransactionEntity.getAndConstructParamValues());
        } catch (Exception e) {
            log.error("[RuntimeSagaTransactionProcess]流程{}异常, 离线恢复异常, 流程类型{}, 业务流水号:{}",
                    sagaTransactionEntity.getSagaTransactionName(), sagaTransactionEntity.getSagaTransactionType(),
                    sagaTransactionEntity.getBizSerialNo(), e);
        } finally {
            SagaTransactionContextHolder.clearSagaTransactionContext();
        }

        log.debug("[RuntimeSagaTransactionProcess]流程{}异常, 离线恢复完成, 流程类型{}, 业务流水号:{}",
                sagaTransactionEntity.getSagaTransactionName(), sagaTransactionEntity.getSagaTransactionType(),
                sagaTransactionEntity.getBizSerialNo());
    }
}
