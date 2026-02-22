package com.hkt.iot.device.interfaces.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.Instant;

/**
 * 设备上线通知
 *
 * @author HKT IoT Team
 */
@Data
public class DeviceOnlineNotification {

    /**
     * 设备ID
     */
    @NotBlank(message = "设备ID不能为空")
    @JsonProperty("deviceId")
    private String deviceId;

    /**
     * 设备类型
     */
    @NotBlank(message = "设备类型不能为空")
    @JsonProperty("deviceType")
    private String deviceType;

    /**
     * 租户ID
     */
    @NotBlank(message = "租户ID不能为空")
    @JsonProperty("tenantId")
    private String tenantId;

    /**
     * 连接时间（毫秒时间戳）
     */
    @JsonProperty("connectedAt")
    private Instant connectedAt;

    /**
     * 连接信息
     */
    @JsonProperty("connectionInfo")
    private ConnectionInfo connectionInfo;

    /**
     * 连接信息
     */
    @Data
    public static class ConnectionInfo {
        /**
         * IP地址
         */
        @JsonProperty("ipAddress")
        private String ipAddress;

        /**
         * 协议
         */
        @JsonProperty("protocol")
        private String protocol;

        /**
         * 客户端版本
         */
        @JsonProperty("clientVersion")
        private String clientVersion;
    }
}
