package com.flow.saga.repository.mapper;

import com.flow.saga.entity.SagaTransactionEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SagaTransactionEntityMapper {
    int insert(SagaTransactionEntity sagaTransactionEntity);

    int insertSelective(SagaTransactionEntity sagaTransactionEntity);

    SagaTransactionEntity selectByPrimaryKey(Long id, Long shardRoutingKey);

    int updateByPrimaryKeySelective(SagaTransactionEntity sagaTransactionEntity);

    int updateByPrimaryKey(SagaTransactionEntity sagaTransactionEntity);
    
    List<SagaTransactionEntity> listSagaTransactionByCreateTimeInBatch(@Param("startTime") Long startTime,
                                                                       @Param("endTime") Long endTime, @Param("limit") Integer limit);

    List<SagaTransactionEntity> listSagaTransactionByCreateTime(Long createTime);
}