package com.huakuangtong.iot.ingestion.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

/**
 * 设备事件消息模型
 *
 * 对应MQTT Topic: device/{tenantId}/{deviceType}/{deviceId}/event
 *
 * @author IoT Expert
 * @since 2026-02-20
 */
@Data
public class EventMessage {

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
     * 设备类型
     */
    @JsonProperty("deviceType")
    private String deviceType;

    /**
     * 事件发生时间戳（毫秒）
     */
    @JsonProperty("timestamp")
    private Long timestamp;

    /**
     * 事件类型
     * 如: alarm, fault, warning, info
     */
    @JsonProperty("eventType")
    private String eventType;

    /**
     * 事件级别
     * INFO, WARN, ERROR
     */
    @JsonProperty("eventLevel")
    private String eventLevel;

    /**
     * 事件数据
     * 根据事件类型不同，包含不同的字段
     */
    @JsonProperty("data")
    private Map<String, Object> data;

    /**
     * 元数据（可选）
     */
    @JsonProperty("metadata")
    private Map<String, Object> metadata;
}
