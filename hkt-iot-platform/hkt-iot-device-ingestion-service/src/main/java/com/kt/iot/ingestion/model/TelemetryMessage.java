package com.hkt.iot.ingestion.model;

import lombok.Data;

import java.util.Map;

/**
 * 设备遥测数据消息模型
 *
 * 对应MQTT Topic: device/{tenantId}/{deviceType}/{deviceId}/telemetry
 *
 * @author IoT Expert
 * @since 2026-02-20
 */
@Data
public class TelemetryMessage {

    /**
     * 消息ID（用于幂等处理）
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
     * 数据上报时间戳（毫秒）
     */
    private Long timestamp;

    /**
     * 遥测数据
     */
    private Map<String, Object> data;

    /**
     * 元数据
     */
    private TelemetryMetadata metadata;

    @Data
    public static class TelemetryMetadata {
        private Integer battery;
        private Integer rssi;
        private String firmwareVersion;
    }
}
