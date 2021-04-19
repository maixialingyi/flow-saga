package com.flow.saga.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "saga")
public class SagaProperties {
    /**
     * 是否异步执行cancel
     */
    private boolean asyncCancel = false;

    /**
     * 是否并发执行Participant的cancel
     */
    private boolean concurrentCancel = false;

    /**
     * 等待并发cancel处理完成的超时时间
     */
    private int waitConcurrentCancelTimeoutInMs = 10000;

    /**
     * 是否保存子事务
     */
    private boolean persistSubTransaction = true;

    private ThreadPoolProperties threadPoolProperties = new ThreadPoolProperties();

    @Data
    public static class ThreadPoolProperties {
        /**
         * corePoolSize
         */
        private int coreSize = 10;

        /**
         * maximumPoolSize
         */
        private int maxSize = 100;
        /**
         * keepAliveTime in ms
         */
        private int keepAliveTimeInMs = 30000;
    }
}
