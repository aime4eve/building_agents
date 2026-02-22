package com.hkt.iot.ingestion.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hkt.iot.ingestion.model.EventMessage;
import com.hkt.iot.ingestion.model.StatusMessage;
import com.hkt.iot.ingestion.model.TelemetryMessage;
import com.hkt.iot.ingestion.service.MessageBridgeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

/**
 * Kafka消息桥接服务实现
 *
 * @author IoT Expert
 * @since 2026-02-20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageBridgeServiceImpl implements MessageBridgeService {

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${kafka.topic.telemetry:device-telemetry}")
    private String telemetryTopic;

    @Value("${kafka.topic.event:device-event}")
    private String eventTopic;

    @Value("${kafka.topic.status:device-status}")
    private String statusTopic;

    @Value("${kafka.topic.alarm:device-alarm}")
    private String alarmTopic;

    @Override
    public void sendTelemetry(TelemetryMessage message, String topic) {
        try {
            String payload = objectMapper.writeValueAsString(message);
            String key = message.getDeviceId();

            sendAsync(payload, telemetryTopic, key);

            log.debug("Telemetry message sent to Kafka, deviceId: {}, topic: {}", key, topic);

        } catch (Exception e) {
            log.error("Failed to send telemetry to Kafka, deviceId: {}", message.getDeviceId(), e);
        }
    }

    @Override
    public void sendEvent(EventMessage message, String topic) {
        try {
            String payload = objectMapper.writeValueAsString(message);
            String key = message.getDeviceId();

            // 根据事件级别选择Topic
            String targetTopic = eventTopic;
            if ("ERROR".equalsIgnoreCase(message.getEventLevel())) {
                targetTopic = alarmTopic;
            }

            sendAsync(payload, targetTopic, key);

            log.debug("Event message sent to Kafka, deviceId: {}, eventType: {}, topic: {}",
                key, message.getEventType(), targetTopic);

        } catch (Exception e) {
            log.error("Failed to send event to Kafka, deviceId: {}", message.getDeviceId(), e);
        }
    }

    @Override
    public void sendStatus(StatusMessage message, String topic) {
        try {
            String payload = objectMapper.writeValueAsString(message);
            String key = message.getDeviceId();

            sendAsync(payload, statusTopic, key);

            log.debug("Status message sent to Kafka, deviceId: {}, status: {}", key, message.getStatus());

        } catch (Exception e) {
            log.error("Failed to send status to Kafka, deviceId: {}", message.getDeviceId(), e);
        }
    }

    @Override
    public void sendRawMessage(String payload, String kafkaTopic, String key) {
        sendAsync(payload, kafkaTopic, key);
    }

    /**
     * 异步发送消息到Kafka
     */
    private void sendAsync(String payload, String topic, String key) {
        ListenableFuture<SendResult<String, String>> future =
            kafkaTemplate.send(topic, key, payload);

        future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
            @Override
            public void onSuccess(SendResult<String, String> result) {
                log.trace("Message sent successfully, topic: {}, partition: {}, offset: {}",
                    topic,
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());
            }

            @Override
            public void onFailure(Throwable ex) {
                log.error("Failed to send message, topic: {}, key: {}, error: {}",
                    topic, key, ex.getMessage());
                // 可以考虑将失败的消息发送到死信队列
            }
        });
    }
}
