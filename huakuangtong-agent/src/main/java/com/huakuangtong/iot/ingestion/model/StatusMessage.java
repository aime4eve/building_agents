package com.huakuangtong.iot.ingestion.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 设备状态消息模型
 *
 * 对应MQTT Topic: device/{tenantId}/{deviceType}/{deviceId}/status
 *
 * @author IoT Expert
 * @since 2026-02-20
 */
@Data
public class StatusMessage {

    /**
     * 消息ID
     */
    @JsonProperty("msgId")
    private String msgId;

    /**
     * 设备ID
     */
    @JsonProperty("deviceId")
    private String deviceId;

    /**
     * 租户ID（从Topic解析）
     */
    @JsonProperty("tenantId")
    private String tenantId;

    /**
     * 设备类型
     */
    @JsonProperty("deviceType")
    private String deviceType;

    /**
     * 状态更新时间戳（毫秒）
     */
    @JsonProperty("timestamp")
    private Long timestamp;

    /**
     * 设备状态
     * ONLINE - 在线
     * OFFLINE - 离线
     * FAULT - 故障
     * MAINTENANCE - 维护中
     */
    @JsonProperty("status")
    private String status;

    /**
     * 状态变更原因（可选）
     */
    @JsonProperty("reason")
    private String reason;

    /**
     * 连接信息（上线时）
     */
    @JsonProperty("connectionInfo")
    private ConnectionInfo connectionInfo;

    /**
     * 连接信息
     */
    @Data
    public static class ConnectionInfo {

        /**
         * 客户端IP地址
         */
        @JsonProperty("ipAddress")
        private String ipAddress;

        /**
         * 连接协议
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
