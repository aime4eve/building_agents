package com.hkt.iot.notification.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serial;
import java.io.Serializable;

/**
 * 通知响应
 *
 * @author HKT IoT Team
 */
@Schema(description = "通知响应")
public class NotificationResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "响应码")
    private String code;

    @Schema(description = "响应消息")
    private String message;

    @Schema(description = "通知请求ID")
    private Long requestId;

    @Schema(description = "时间戳")
    private Long timestamp;

    public NotificationResponse() {
        this.timestamp = System.currentTimeMillis();
    }

    public NotificationResponse(String code, String message, Long requestId) {
        this.code = code;
        this.message = message;
        this.requestId = requestId;
        this.timestamp = System.currentTimeMillis();
    }

    public static NotificationResponse success(Long requestId) {
        return new NotificationResponse("200", "success", requestId);
    }

    public static NotificationResponse success(String message, Long requestId) {
        return new NotificationResponse("200", message, requestId);
    }

    public static NotificationResponse error(String code, String message) {
        return new NotificationResponse(code, message, null);
    }

    public boolean isSuccess() {
        return "200".equals(this.code);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
