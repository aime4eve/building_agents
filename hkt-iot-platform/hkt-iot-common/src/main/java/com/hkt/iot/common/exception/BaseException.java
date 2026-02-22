package com.hkt.iot.common.exception;

import lombok.Getter;

import java.io.Serial;

/**
 * 基础异常类
 * 所有自定义异常的父类
 *
 * @author HKT IoT Team
 */
@Getter
public class BaseException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private final String code;

    /**
     * 错误消息
     */
    private final String message;

    /**
     * 错误详情（用于开发调试）
     */
    private final String detail;

    public BaseException(String code, String message) {
        super(message);
        this.code = code;
        this.message = message;
        this.detail = null;
    }

    public BaseException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
        this.detail = cause != null ? cause.getMessage() : null;
    }

    public BaseException(String code, String message, String detail) {
        super(message);
        this.code = code;
        this.message = message;
        this.detail = detail;
    }

    public BaseException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
        this.detail = null;
    }

    public BaseException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
        this.detail = cause != null ? cause.getMessage() : null;
    }

    public BaseException(ErrorCode errorCode, String detail) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
        this.detail = detail;
    }
}
