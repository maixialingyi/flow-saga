package com.flow.saga.entity;

public interface SagaSubTransaction<T> {
    /**
     * 子事务处理流程
     *
     * @param sagaContext SagaFlow上下文
     */
    void process(String bizSerialNo, T sagaContext) throws Exception;

    /**
     * 子事务处理失败回滚流程
     *
     * @param sagaContext SagaFlow上下文
     */
    void rollback(String bizSerialNo, T sagaContext);

    /**
     * 子事务执行成功后操作
     *
     * @param sagaContext SagaFlow上下文
     */
    void success(String bizSerialNo, T sagaContext);

    /**
     * 子事务执行失败后操作，可以在这个方法里保证子事务失败的事务性
     *
     * @param sagaContext SagaFlow上下文
     */
    void fail(String bizSerialNo, T sagaContext);
}
