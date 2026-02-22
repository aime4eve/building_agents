package com.hkt.iot.workflow.exception;

/**
 * 业务异常
 * 用于处理业务逻辑中的可预期异常，不中断流程执行
 *
 * @author HKT IoT Team
 */
public class BusinessException extends RuntimeException {

    private final String errorCode;

    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public BusinessException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
