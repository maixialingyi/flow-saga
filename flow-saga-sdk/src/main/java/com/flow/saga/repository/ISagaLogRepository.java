package com.flow.saga.repository;

import com.flow.saga.entity.SagaSubTransactionEntity;
import com.flow.saga.entity.SagaTransactionEntity;
import java.util.List;

public interface ISagaLogRepository {

    boolean saveSagaTransactionEntity(SagaTransactionEntity sagaTransactionEntity);

    boolean saveSagaSubTransactionEntity(SagaSubTransactionEntity sagaSubTransactionEntity);

    boolean updateSagaTransactionEntity(SagaTransactionEntity sagaTransactionEntity);

    boolean updateSagaSubTransactionEntity(SagaSubTransactionEntity sagaSubTransactionEntity);

    SagaTransactionEntity querySagaTransactionById(Long sagaTransactionId, Long shardRoutingKey);

    List<SagaSubTransactionEntity> querySagaSubTransactionsById(Long sagaTransactionId, Long shardRoutingKey);

    /**
     * 分批查询创建时间范围的SagaTransactionEntity
     *
     * @param startTime
     * @param endTime
     * @param limit
     * @return
     */
    List<SagaTransactionEntity> querySagaTransactionByCreateTimeInBatch(Long startTime, Long endTime, Integer limit);

    /**
     * 按照创建时间查询分批查询创建时间范围的SagaTransactionEntity
     *
     * @param createTime
     * @return
     */
    List<SagaTransactionEntity> querySagaTransactionByCreateTime(Long createTime);
}
