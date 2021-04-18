package com.flow.saga.exception;

public class SagaFlowSystemException extends RuntimeException {

    public SagaFlowSystemException() {
    }


    public SagaFlowSystemException(String message) {
        super(message);
    }


    public SagaFlowSystemException(String message, Throwable cause) {
        super(message, cause);
    }


    public SagaFlowSystemException(Throwable cause) {
        super(cause);
    }

    public SagaFlowSystemException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
