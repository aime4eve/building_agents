package com.hkt.iot.ingestion.model;

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
     * 事件发生时间戳（毫秒）
     */
    private Long timestamp;

    /**
     * 事件类型
     */
    private String eventType;

    /**
     * 事件级别
     */
    private String eventLevel;

    /**
     * 事件数据
     */
    private Map<String, Object> data;

    /**
     * 元数据
     */
    private Map<String, Object> metadata;
}
