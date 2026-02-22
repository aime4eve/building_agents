package com.hkt.iot.rule.domain.model;

/**
 * 动作执行结果
 *
 * @author AI Engineer
 * @since 1.0.0
 */
public class ActionResult {
    private final boolean success;
    private final String message;
    private final Object data;
    private final String errorCode;

    private ActionResult(boolean success, String message, Object data, String errorCode) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.errorCode = errorCode;
    }

    /**
     * 成功结果
     */
    public static ActionResult success() {
        return new ActionResult(true, "Action executed successfully", null, null);
    }

    /**
     * 成功结果（带消息）
     */
    public static ActionResult success(String message) {
        return new ActionResult(true, message, null, null);
    }

    /**
     * 成功结果（带数据）
     */
    public static ActionResult success(String message, Object data) {
        return new ActionResult(true, message, data, null);
    }

    /**
     * 失败结果
     */
    public static ActionResult failed(String errorMessage) {
        return new ActionResult(false, errorMessage, null, null);
    }

    /**
     * 失败结果（带错误码）
     */
    public static ActionResult failed(String errorCode, String errorMessage) {
        return new ActionResult(false, errorMessage, null, errorCode);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
