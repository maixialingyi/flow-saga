package com.flow.saga.aspect.Manager;

import com.flow.saga.aspect.exceptionhandler.SagaTransactionExceptionHandlerDispatcher;
import com.flow.saga.configuration.SagaProperties;
import com.flow.saga.entity.*;
import com.flow.saga.repository.SagaLogRepository;
import com.flow.saga.utils.BeanUtil;
import com.flow.saga.utils.JsonUtil;
import com.google.common.util.concurrent.Uninterruptibles;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class BaseSagaTransactionManager {

    @Resource
    private SagaProperties sagaProperties;

    private ExecutorService executorService;

    @Resource
    private SagaLogRepository sagaLogRepository;

    @Resource
    private SagaTransactionExceptionHandlerDispatcher sagaTransactionExceptionHandlerDispatcher;

    public void commit(SagaTransactionContext sagaTransactionContext) {
        // 如果不是最外层事务，就直接返回，不提交事务。
        if (!isLastTransaction(sagaTransactionContext)) {
            return;
        }

        SagaTransactionEntity sagaTransactionEntity = sagaTransactionContext.getSagaTransactionEntity();
        sagaTransactionEntity.success();
        // 执行主事务成功后方法
        this.successTransactionProcess(sagaTransactionEntity);
        // 修改主，子事务状态为执行成功
        this.updateSagaTransaction(sagaTransactionEntity);
        log.debug("[SagaTransactionProcess]流程{}结束, 流程类型{}, 业务流水号:{}",
                sagaTransactionEntity.getSagaTransactionName(), sagaTransactionEntity.getSagaTransactionType(),
                sagaTransactionEntity.getBizSerialNo());
    }

    public void handleException(SagaTransactionContext context, Exception e) {
        // 如果是内层的事务就直接返回
        if (!isLastTransaction(context)) {
            return;
        }
        if (sagaProperties.isAsyncCancel()) {
            this.handleExceptionAsyn(context, e);
        } else {
            this.handleExceptionSyn(context, e);
        }
    }

    public void handleExceptionSyn(SagaTransactionContext context, Exception e) {
        SagaTransactionEntity sagaTransactionEntity = context.getSagaTransactionEntity();
        try {
            sagaTransactionExceptionHandlerDispatcher.handleException(context, e);
        } catch (Exception e1) {
            // 框架处理失败产生异常
            log.error("[SagaSubTransactionProcess]流程{}异常, 框架处理失败, 流程类型{}, 业务流水号:{}",
                    sagaTransactionEntity.getSagaTransactionName(), sagaTransactionEntity.getSagaTransactionType(),
                    sagaTransactionEntity.getBizSerialNo(), e1);
        }
        log.debug("[SagaSubTransactionProcess]流程{}异常, 异常处理结束, 流程类型{}, 业务流水号:{}",
                sagaTransactionEntity.getSagaTransactionName(), sagaTransactionEntity.getSagaTransactionType(),
                sagaTransactionEntity.getBizSerialNo());
        // 执行失败更新saga flow上下文
        this.updateSagaTransaction(sagaTransactionEntity);
        // 执行失败方法
        this.failTransactionProcess(sagaTransactionEntity, e);

    }

    public void handleExceptionAsyn(SagaTransactionContext context, Exception e) {
        // 如果是内层的事务就直接返回
        if (!isLastTransaction(context)) {
            return;
        }

        executorService.submit(() -> {
            SagaTransactionEntity sagaTransactionEntity = context.getSagaTransactionEntity();
            try {
                sagaTransactionExceptionHandlerDispatcher.handleException(context, e);
            } catch (Exception e1) {
                // 框架处理失败产生异常
                log.error("[SagaSubTransactionProcess]流程{}异常, 框架处理失败, 流程类型{}, 业务流水号:{}",
                        sagaTransactionEntity.getSagaTransactionName(), sagaTransactionEntity.getSagaTransactionType(),
                        sagaTransactionEntity.getBizSerialNo(), e1);
            }
            // 执行失败方法
            this.failTransactionProcess(sagaTransactionEntity, e);
            // 执行失败更新saga flow上下文
            this.updateSagaTransaction(sagaTransactionEntity);
            log.debug("[SagaSubTransactionProcess]流程{}异常, 异步异常处理结束, 流程类型{}, 业务流水号:{}",
                    sagaTransactionEntity.getSagaTransactionName(), sagaTransactionEntity.getSagaTransactionType(),
                    sagaTransactionEntity.getBizSerialNo());
        });

    }

    public void commitSubTransaction(SagaSubTransactionEntity sagaSubTransactionEntity, Object object) {
        sagaSubTransactionEntity.setReturnValue(object);
        sagaSubTransactionEntity.setReturnValueJson(JsonUtil.toJson(object));
        sagaSubTransactionEntity.success();
        this.updateSagaSubTransaction(sagaSubTransactionEntity);

        log.debug("[SagaSubTransactionProcess]子流程{}结束, 业务流水号:{}",
                sagaSubTransactionEntity.getSubTransactionName(), sagaSubTransactionEntity.getBizSerialNo());
    }

    public Object handleSubTransactionException(SagaTransactionContext context,
                                                SagaSubTransactionEntity sagaSubTransactionEntity, ProceedingJoinPoint joinPoint, Throwable e)
            throws Throwable {

        log.warn("[SagaSubTransactionProcess]子流程{}执行失败，异常处理, 业务流水号:{}",
                sagaSubTransactionEntity.getSubTransactionName(), sagaSubTransactionEntity.getBizSerialNo(), e);
        // 是否需要重试判断
        SagaTransactionEntity sagaTransactionEntity = context.getSagaTransactionEntity();
        while (context.needRetryCheckAndAddRetryTime((Exception) e)) {
            // 重试时间间隔
            Uninterruptibles.sleepUninterruptibly(context.getSagaTransactionConfig().getRetryInterval(),
                    TimeUnit.MILLISECONDS);
            try {
                // 重试成功直接返回
                log.debug("[SagaSubTransactionProcess]子流程{}重试执行开始，执行次数{}, 业务流水号:{}",
                        sagaSubTransactionEntity.getSubTransactionName(), sagaTransactionEntity.getRetryTime(),
                        sagaSubTransactionEntity.getBizSerialNo());
                Object returnValue = joinPoint.proceed();
                sagaSubTransactionEntity.success();
                this.successUpdateSagaSubTransaction(sagaSubTransactionEntity);
                return returnValue;
            } catch (Throwable e1) {
                e = e1;
                log.warn("[SagaSubTransactionProcess]子流程{}执行重试失败，执行次数{}, 业务流水号:{}",
                        sagaSubTransactionEntity.getSubTransactionName(), sagaTransactionEntity.getRetryTime(),
                        sagaSubTransactionEntity.getBizSerialNo(), e);
            }
        }
        // 超过最大重试次数后，保存子事务执行失败结果
        sagaSubTransactionEntity.fail(e.getMessage());
        this.updateSagaSubTransaction(sagaSubTransactionEntity);

        log.warn("[SagaSubTransactionProcess]子流程{}执行异常,  业务流水号:{}",
                sagaSubTransactionEntity.getSubTransactionName(), sagaSubTransactionEntity.getBizSerialNo(), e);

        throw e;
    }

    public void addSubTransaction(SagaTransactionContext context, SagaSubTransactionEntity sagaSubTransactionEntity) {
        context.getSagaTransactionEntity().addSubTransaction(sagaSubTransactionEntity);
        context.setCurrentSagaSubTransaction(sagaSubTransactionEntity);
        this.saveSagaSubTransaction(sagaSubTransactionEntity);
        log.debug("[SagaSubTransactionProcess]子流程{}开始, 业务流水号:{}",
                sagaSubTransactionEntity.getSubTransactionName(), sagaSubTransactionEntity.getBizSerialNo());
    }

    /**
     * 是否为恢复模式
     * 主事务被其他事务嵌套   恢复模式   上下文 != null  recover = true
     *                    非恢复模式 上下文 != null  recover = false
     * 主事务未被其他事务嵌套  恢复模式  上下文 != null  recover = true
     *                    非恢复模式 上下文 == null  recover = false
     */
    public boolean isRecover() {
        return SagaTransactionContextHolder.getSagaTransactionContext() != null
                && SagaTransactionContextHolder.getSagaTransactionContext().isRecover();
    }

    public boolean isInSagaTransaction() {
        return SagaTransactionContextHolder.getSagaTransactionContext() != null;
    }

    /**
     * 判断当前事务是否是最后的
     */
    boolean isLastTransaction(SagaTransactionContext sagaTransactionContext) {
        return sagaTransactionContext != null && sagaTransactionContext.getLayerCount() == 1;
    }

    //
    void successTransactionProcess(SagaTransactionEntity sagaTransactionEntity) {
        try {
            InvocationContext successInvocation = sagaTransactionEntity.getAndConstructSuccessInvocationContext();
            if (successInvocation == null) {
                return;
            }
            Object service = BeanUtil.getBean(successInvocation.getTargetClass());
            ReflectionUtils.invokeMethod(successInvocation.getMethod(), service,sagaTransactionEntity.getAndConstructParamValues());
        } catch (Exception e) {
            log.error("[SagaSubTransactionProcess]流程{}, 框架处理成功，执行成功方法失败, 流程类型{}, 业务流水号:{}",
                    sagaTransactionEntity.getSagaTransactionName(), sagaTransactionEntity.getSagaTransactionType(),
                    sagaTransactionEntity.getBizSerialNo(), e);
        }
    }

    void failTransactionProcess(SagaTransactionEntity sagaTransactionEntity, Exception e) {
        try {
            InvocationContext failInvocationContext = sagaTransactionEntity.getAndConstructFailInvocationContext();
            if (failInvocationContext == null) {
                return;
            }
            Object service = BeanUtil.getBean(failInvocationContext.getTargetClass());
            Object[] params = sagaTransactionEntity.getAndConstructParamValues();
            Object[] paramsWithException;
            if (params == null || params.length == 0) {
                paramsWithException= new Object[]{e};
            } else {
                paramsWithException = ArrayUtils.add(params, e);
            }
            ReflectionUtils.invokeMethod(failInvocationContext.getMethod(), service, paramsWithException);
        } catch (Exception ee) {
            log.error("[SagaSubTransactionProcess]流程{}, 框架处理失败，执行失败方法失败, 流程类型{}, 业务流水号:{}",
                    sagaTransactionEntity.getSagaTransactionName(), sagaTransactionEntity.getSagaTransactionType(),
                    sagaTransactionEntity.getBizSerialNo(), ee);
        }
    }

    public void successSubTransactionProcess(SagaSubTransactionEntity sagaSubTransactionEntity) {
        try {
            InvocationContext successInvocationContext = sagaSubTransactionEntity
                    .getAndConstructSuccessInvocationContext();
            if (successInvocationContext == null) {
                return;
            }
            Object service = BeanUtil.getBean(successInvocationContext.getTargetClass());
            ReflectionUtils.invokeMethod(successInvocationContext.getMethod(), service,
                    sagaSubTransactionEntity.getAndConstructParamValues());
        } catch (Exception e) {
            log.error("[SagaSubTransactionProcess]子流程{}, 框架处理成功，执行成功方法失败, 业务流水号:{}",
                    sagaSubTransactionEntity.getSubTransactionName(), sagaSubTransactionEntity.getBizSerialNo(), e);
        }

    }

    public void failSubTransactionProcess(SagaSubTransactionEntity sagaSubTransactionEntity, Exception e) {
        try {
            InvocationContext failInvocationContext = sagaSubTransactionEntity.getAndConstructFailInvocationContext();
            if (failInvocationContext == null) {
                return;
            }
            Object service = BeanUtil.getBean(failInvocationContext.getTargetClass());
            Object[] params = sagaSubTransactionEntity.getAndConstructParamValues();
            Object[] paramsWithException;
            if (params == null || params.length == 0) {
                paramsWithException= new Object[]{e};
            } else {
                paramsWithException = ArrayUtils.add(params, e);
            }
            ReflectionUtils.invokeMethod(failInvocationContext.getMethod(), service, paramsWithException);
        } catch (Exception ee) {
            log.error("[SagaSubTransactionProcess]子流程{}, 框架处理失败，执行失败方法失败, 业务流水号:{}",
                    sagaSubTransactionEntity.getSubTransactionName(), sagaSubTransactionEntity.getBizSerialNo(), ee);
        }

    }

    void updateSagaTransaction(SagaTransactionEntity sagaTransactionEntity) {
        List<SagaSubTransactionEntity> sagaSubTransactionEntities = sagaTransactionEntity.getSagaSubTransactionEntities();

        // 业务流程执行成功时，不保存入参到DB
        if (sagaTransactionEntity.isFinish()) {
            sagaTransactionEntity.successClearParam();
            sagaSubTransactionEntities.forEach(a -> {
                a.clearParam();
                a.clearReturn();
            });
        }
        sagaSubTransactionEntities.forEach(this::updateSagaSubTransaction);

        try {
            sagaLogRepository.updateSagaTransactionEntity(sagaTransactionEntity);
        } catch (Exception e) {
            String message = MessageFormatter.format("[SagaSubTransactionProcess]流程{}更新失败, 流程类型{}, 业务流水号:{}",
                    new Object[] { sagaTransactionEntity.getSagaTransactionName(),
                            sagaTransactionEntity.getSagaTransactionType(), sagaTransactionEntity.getBizSerialNo() })
                    .getMessage();
            log.error(message, e);
        }
    }

    void saveSagaTransaction(SagaTransactionEntity sagaTransactionEntity) {
        try {
            sagaLogRepository.saveSagaTransactionEntity(sagaTransactionEntity);
        } catch (Exception e) {
            String message = MessageFormatter.format("[SagaTransactionProcess]流程{}初始化失败, 流程类型{}, 业务流水号:{}",
                    new Object[] { sagaTransactionEntity.getSagaTransactionName(),
                            sagaTransactionEntity.getSagaTransactionType(), sagaTransactionEntity.getBizSerialNo() })
                    .getMessage();
            // 一致性保障组件异常，不影响业务，需要报警。
            log.error(message, e);
        }
    }

    void successUpdateSagaSubTransaction(SagaSubTransactionEntity sagaSubTransactionEntity) {
        sagaSubTransactionEntity.clearParam();
        this.updateSagaSubTransaction(sagaSubTransactionEntity);
    }

    void updateSagaSubTransaction(SagaSubTransactionEntity sagaSubTransactionEntity) {
        if (!sagaProperties.isPersistSubTransaction()) {
            return;
        }
        try {
            sagaLogRepository.updateSagaSubTransactionEntity(sagaSubTransactionEntity);
        } catch (Exception e) {
            String message = MessageFormatter.format("[SagaSubTransactionProcess]子流程{}更新失败, 业务流水号:{}",
                    new Object[] { sagaSubTransactionEntity.getSubTransactionName(),
                            sagaSubTransactionEntity.getBizSerialNo() })
                    .getMessage();
            log.error(message, e);
        }
    }

    void saveSagaSubTransaction(SagaSubTransactionEntity sagaSubTransactionEntity) {
        if (!sagaProperties.isPersistSubTransaction()) {
            return;
        }
        try {
            sagaLogRepository.saveSagaSubTransactionEntity(sagaSubTransactionEntity);
        } catch (Exception e) {
            String message = MessageFormatter.format("[SagaSubTransactionProcess]子流程{}初始化失败, 业务流水号:{}",
                    new Object[] { sagaSubTransactionEntity.getSubTransactionName(),
                            sagaSubTransactionEntity.getBizSerialNo() })
                    .getMessage();
            log.error(message, e);
        }
    }

    @PostConstruct
    public void setProperties() {
        SagaProperties.ThreadPoolProperties threadPoolProperties = sagaProperties.getThreadPoolProperties();
        executorService = new ThreadPoolExecutor(threadPoolProperties.getCoreSize(), threadPoolProperties.getMaxSize(),
                threadPoolProperties.getKeepAliveTimeInMs(), TimeUnit.MILLISECONDS, new SynchronousQueue<>(),
                new DefaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());
    }

    static class DefaultThreadFactory implements ThreadFactory {
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        DefaultThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            namePrefix = "component-thread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }
}
