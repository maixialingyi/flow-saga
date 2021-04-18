package com.flow.saga.repository.mapper;

import com.flow.saga.entity.SagaSubTransactionEntity;
import java.util.List;

public interface SagaSubTransactionEntityMapper {

    int insert(SagaSubTransactionEntity record);

    int insertSelective(SagaSubTransactionEntity record);

    SagaSubTransactionEntity selectByPrimaryKey(Long id, Long shardRoutingKey);

    List<SagaSubTransactionEntity> selectBySagaTransactionId(Long sagaTransactionId, Long shardRoutingKey);

    int updateByPrimaryKeySelective(SagaSubTransactionEntity record);

    int updateByPrimaryKey(SagaSubTransactionEntity record);
}
