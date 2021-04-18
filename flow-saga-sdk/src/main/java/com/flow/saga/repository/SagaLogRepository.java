package com.flow.saga.repository;

import com.flow.saga.entity.SagaSubTransactionEntity;
import com.flow.saga.entity.SagaTransactionEntity;
import com.flow.saga.repository.mapper.SagaSubTransactionEntityMapper;
import com.flow.saga.repository.mapper.SagaTransactionEntityMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Repository
public class SagaLogRepository implements ISagaLogRepository {

    @Resource
    private SagaTransactionEntityMapper sagaTransactionEntityMapper;

    @Resource
    private SagaSubTransactionEntityMapper sagaSubTransactionEntityMapper;

    @Autowired
    @Qualifier("sagaLeafIdGenerator")
    private IdGenerator sagaLeafIdGenerator;

    @Override
    public boolean saveSagaTransactionEntity(SagaTransactionEntity sagaTransactionEntity) {
        sagaTransactionEntity.setId(sagaLeafIdGenerator.nextId());
        sagaTransactionEntity.fillShardRoutingKeyIfAbsent();
        Long now = System.currentTimeMillis();
        sagaTransactionEntity.setCreateTime(now);
        sagaTransactionEntity.setUpdateTime(now);
        sagaTransactionEntity.setVersion(0);
        sagaTransactionEntityMapper.insert(sagaTransactionEntity);
        return true;
    }

    @Override
    public boolean saveSagaSubTransactionEntity(SagaSubTransactionEntity sagaSubTransactionEntity) {
        sagaSubTransactionEntity.setId(sagaLeafIdGenerator.nextId());
        sagaSubTransactionEntity.fillShardRoutingKeyIfAbsent();
        Long now = System.currentTimeMillis();
        sagaSubTransactionEntity.setCreateTime(now);
        sagaSubTransactionEntity.setUpdateTime(now);
        sagaSubTransactionEntity.setVersion(0);
        sagaSubTransactionEntityMapper.insert(sagaSubTransactionEntity);
        return true;
    }

    @Override
    public boolean updateSagaTransactionEntity(SagaTransactionEntity sagaTransactionEntity) {
        Long now = System.currentTimeMillis();
        sagaTransactionEntity.setUpdateTime(now);
        sagaTransactionEntity.setVersion(sagaTransactionEntity.getVersion() + 1);
        sagaTransactionEntityMapper.updateByPrimaryKeySelective(sagaTransactionEntity);
        return true;
    }

    @Override
    public boolean updateSagaSubTransactionEntity(SagaSubTransactionEntity sagaSubTransactionEntity) {
        Long now = System.currentTimeMillis();
        sagaSubTransactionEntity.setUpdateTime(now);
        sagaSubTransactionEntity.setVersion(sagaSubTransactionEntity.getVersion() + 1);
        sagaSubTransactionEntityMapper.updateByPrimaryKeySelective(sagaSubTransactionEntity);
        return true;
    }

    @Override
    public SagaTransactionEntity querySagaTransactionById(Long sagaTransactionId, Long shardRoutingKey) {
        return sagaTransactionEntityMapper.selectByPrimaryKey(sagaTransactionId, shardRoutingKey);
    }

    @Override
    public List<SagaSubTransactionEntity> querySagaSubTransactionsById(Long sagaTransactionId, Long shardRoutingKey) {
        return sagaSubTransactionEntityMapper.selectBySagaTransactionId(sagaTransactionId, shardRoutingKey);
    }

    /**
     * 分批查询创建时间范围的SagaTransactionEntity
     *
     * @param startTime
     * @param endTime
     * @param limit
     * @return
     */
    @Override
    public List<SagaTransactionEntity> querySagaTransactionByCreateTimeInBatch(Long startTime, Long endTime,
                                                                               Integer limit) {
        return sagaTransactionEntityMapper.listSagaTransactionByCreateTimeInBatch(startTime, endTime, limit);
    }

    /**
     * 按照创建时间查询分批查询创建时间范围的SagaTransactionEntity
     *
     * @param createTime
     * @return
     */
    @Override
    public List<SagaTransactionEntity> querySagaTransactionByCreateTime(Long createTime) {
        return sagaTransactionEntityMapper.listSagaTransactionByCreateTime(createTime);
    }

}
