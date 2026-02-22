package com.hkt.iot.device.interfaces.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.Instant;

/**
 * 设备离线通知
 *
 * @author HKT IoT Team
 */
@Data
public class DeviceOfflineNotification {

    /**
     * 设备ID
     */
    @NotBlank(message = "设备ID不能为空")
    @JsonProperty("deviceId")
    private String deviceId;

    /**
     * 租户ID
     */
    @NotBlank(message = "租户ID不能为空")
    @JsonProperty("tenantId")
    private String tenantId;

    /**
     * 断开连接时间（毫秒时间戳）
     */
    @JsonProperty("disconnectedAt")
    private Instant disconnectedAt;

    /**
     * 离线原因
     * 可能的值: timeout, normal, error, kicked
     */
    @JsonProperty("reason")
    private String reason;

    /**
     * 最后通信时间（毫秒时间戳）
     */
    @JsonProperty("lastCommunicatedAt")
    private Instant lastCommunicatedAt;
}
