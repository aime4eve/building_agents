package com.hkt.iot.ingestion.model;

import lombok.Data;

/**
 * 设备状态消息模型
 *
 * @author IoT Expert
 * @since 2026-02-20
 */
@Data
public class StatusMessage {

    /**
     * 消息ID
     */
    private String msgId;

    /**
     * 设备ID
     */
    private String deviceId;

    /**
     * 设备类型
     */
    private String deviceType;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 状态更新时间戳（毫秒）
     */
    private Long timestamp;

    /**
     * 设备状态
     */
    private String status;

    /**
     * 状态变更原因
     */
    private String reason;

    /**
     * 连接信息
     */
    private ConnectionInfo connectionInfo;

    @Data
    public static class ConnectionInfo {
        private String ipAddress;
        private String protocol;
        private String protocolVersion;
        private String clientVersion;
    }
}
