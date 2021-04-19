package com.flow.saga.recover;

import com.flow.saga.entity.SagaTransactionEntity;
import com.flow.saga.repository.SagaLogRepository;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.annotation.Resource;
import javax.management.ObjectName;
import java.util.List;

/**
 * saga离线补偿消息监听器
 */
@Slf4j
@Resource
public class SagaCompensateMessageListenerDemo {

    @Resource
    private SagaTransactionRecoverService sagaTransactionRecoverService;
    @Resource
    private SagaLogRepository sagaLogRepository;

    //消费消息方法
    public Object recvMessage() {
        // 获取消息并解析
        String message = "";
        Long sagaTransactionId = 0l;
        Long shardRoutingKey = 0l;
        try {
            SagaTransactionEntity sagaTransactionEntity = sagaLogRepository.querySagaTransactionById(sagaTransactionId, shardRoutingKey);
            // 数据库无数据、已经处理到终态、或者非本服务的sagaTransactionName -> 不处理
            if (sagaTransactionEntity == null || sagaTransactionEntity.isFinish() || !SagaTransactionNameConstant.sagaTransactionNameList
                    .contains(sagaTransactionEntity.getSagaTransactionName())) {
                return new Object();
            }

            sagaTransactionRecoverService.recover(sagaTransactionEntity);
            return "SUCCESS";
        } catch (Exception e) {
            log.error("[Receive saga离线补偿消息]补偿处理失败: {}, 失败原因:{},stackTrace={}", message, e.getMessage(), ExceptionUtils.getStackTrace(e));
            return "LATER";
        }

    }

    public static class SagaTransactionNameConstant {

        public static final String LECHECK = "LecheckLoanBizService.lecheck";
        public static final String LECHECK_AFTER_FREEZE_LIMIT = "LecheckAfterFreezeLimitBizService.afterFreezeLimit";
        public static final String LECHECK_OPEN_ACCOUNT = "开户流程";
        public static final String FUND_PARTY_APPLY = "资方授信申请";

        public static final List<String> sagaTransactionNameList = Lists.newArrayList();

        static {
            sagaTransactionNameList.add(LECHECK);
            sagaTransactionNameList.add(LECHECK_AFTER_FREEZE_LIMIT);
            sagaTransactionNameList.add(LECHECK_OPEN_ACCOUNT);
            sagaTransactionNameList.add(FUND_PARTY_APPLY);
        }
    }
}
