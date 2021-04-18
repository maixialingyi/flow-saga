package com.flow.saga.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("sagaLeafIdGenerator")
public class LeafIdGenerator implements IdGenerator {

    private static final int INVOKE_RETRY_TIME = 3;

    //临时用
    @Override
    public Long nextId() {
        return System.currentTimeMillis();
    }

    /** leadId 获取 */
    /*@Resource
    private IDGen.Iface leafIdGenlient;

    @Value("${saga.leaf.key}")
    private String leafkey;

    @Override
    public Long nextId() {
        int retryTime = 0;
        TException tException = null;
        while (++retryTime <= INVOKE_RETRY_TIME) {
            try {
                Result result = leafIdGenlient.get(leafkey);
                if (Status.SUCCESS.equals(result.getStatus())) {
                    return result.getId();
                }
                log.warn("[Saga-LeafIdGenerator]获取leaf id失败，第{}次，result{}", retryTime, result);
            } catch (TException e) {
                log.warn("[Saga-LeafIdGenerator]获取leaf id异常，第{}次", retryTime, e);
                tException = e;
            }
        }
        throw new RuntimeException("[Saga-LeafIdGenerator]获取leaf id最终异常", tException);
    }*/


}
