package com.hkt.iot.common.exception;

/**
 * 业务异常
 * 用于业务逻辑异常，如资源不存在、状态错误等
 *
 * @author HKT IoT Team
 */
public class BizException extends BaseException {

    public BizException(String code, String message) {
        super(code, message);
    }

    public BizException(ErrorCode errorCode) {
        super(errorCode);
    }

    public BizException(ErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }

    public BizException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    /**
     * 快速创建业务异常
     *
     * @param code    错误码
     * @param message 错误消息
     * @param args    消息参数（用于String.format）
     * @return 业务异常
     */
    public static BizException of(String code, String message, Object... args) {
        return new BizException(code, String.format(message, args));
    }

    /**
     * 快速创建业务异常
     *
     * @param errorCode 错误码枚举
     * @param args      消息参数（用于String.format）
     * @return 业务异常
     */
    public static BizException of(ErrorCode errorCode, Object... args) {
        return new BizException(errorCode.getCode(), String.format(errorCode.getMessage(), args));
    }
}
