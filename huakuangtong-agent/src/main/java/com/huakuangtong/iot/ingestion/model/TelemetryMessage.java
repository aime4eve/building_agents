package com.huakuangtong.iot.ingestion.model;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("msgId")
    private String msgId;

    /**
     * 设备ID
     */
    @JsonProperty("deviceId")
    private String deviceId;

    /**
     * 设备类型
     */
    @JsonProperty("deviceType")
    private String deviceType;

    /**
     * 数据上报时间戳（毫秒）
     */
    @JsonProperty("timestamp")
    private Long timestamp;

    /**
     * 遥测数据
     * 根据设备类型不同，包含不同的字段
     *
     * 温度传感器: {temperature: 25.5, humidity: 60.2}
     * 水表: {totalVolume: 12345.6, flowRate: 12.5}
     * 电表: {totalEnergy: 12345.6, voltage: 220.5, current: 5.2, power: 1146.6}
     */
    @JsonProperty("data")
    private Map<String, Object> data;

    /**
     * 元数据（可选）
     * 包含设备状态信息，如电池电量、信号强度等
     */
    @JsonProperty("metadata")
    private TelemetryMetadata metadata;

    /**
     * 遥测数据元数据
     */
    @Data
    public static class TelemetryMetadata {

        /**
         * 电池电量（%）
         */
        @JsonProperty("battery")
        private Integer battery;

        /**
         * 信号强度（dBm）
         */
        @JsonProperty("rssi")
        private Integer rssi;

        /**
         * 固件版本
         */
        @JsonProperty("firmwareVersion")
        private String firmwareVersion;
    }
}
