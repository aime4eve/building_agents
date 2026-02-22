package com.hkt.iot.ingestion.service;

import com.hkt.iot.ingestion.model.TelemetryMessage;
import com.hkt.iot.ingestion.model.EventMessage;
import com.hkt.iot.ingestion.model.StatusMessage;

/**
 * Kafka消息桥接服务
 *
 * 负责将MQTT消息桥接到Kafka集群
 *
 * @author IoT Expert
 * @since 2026-02-20
 */
public interface MessageBridgeService {

    /**
     * 发送遥测数据到Kafka
     *
     * @param message 遥测消息
     * @param topic 源MQTT Topic
     */
    void sendTelemetry(TelemetryMessage message, String topic);

    /**
     * 发送设备事件到Kafka
     *
     * @param message 事件消息
     * @param topic 源MQTT Topic
     */
    void sendEvent(EventMessage message, String topic);

    /**
     * 发送设备状态到Kafka
     *
     * @param message 状态消息
     * @param topic 源MQTT Topic
     */
    void sendStatus(StatusMessage message, String topic);

    /**
     * 发送原始消息到Kafka
     *
     * @param payload 消息内容
     * @param kafkaTopic Kafka目标Topic
     * @param key 消息Key（用于分区）
     */
    void sendRawMessage(String payload, String kafkaTopic, String key);
}
