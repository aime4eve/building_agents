package com.hkt.iot.workflow.exception;

/**
 * 系统异常
 * 用于处理系统级别的异常，会中断流程执行并抛出BPMN错误
 *
 * @author HKT IoT Team
 */
public class SystemException extends RuntimeException {

    private final String errorCode;

    public SystemException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public SystemException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
