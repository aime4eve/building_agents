package com.hkt.iot.device.application.event.kafka.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hkt.iot.domain.event.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka事件生产者
 * 负责将领域事件发布到Kafka事件总线
 *
 * @author HKT IoT Team
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 发布领域事件到Kafka
     */
    @Transactional
    public void publishEvent(DomainEvent event) {
        try {
            // 序列化事件
            String eventJson = objectMapper.writeValueAsString(event);

            // 构建消息键（使用租户ID确保同一租户事件有序）
            String messageKey = extractTenantId(event);

            // 发送到Kafka
            CompletableFuture<SendResult<String, String>> future =
                    kafkaTemplate.send(getTopic(event.eventType()), messageKey, eventJson);

            // 异步处理发送结果
            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Kafka事件发送失败: eventType={}, error={}",
                            event.eventType(), ex.getMessage(), ex);
                } else {
                    log.debug("Kafka事件发送成功: eventType={}, partition={}, offset={}",
                            event.eventType(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                }
            });

        } catch (Exception e) {
            log.error("领域事件序列化失败: eventType={}, error={}",
                    event.eventType(), e.getMessage(), e);
            throw new RuntimeException("事件发布失败", e);
        }
    }

    /**
     * 根据事件类型获取主题
     */
    private String getTopic(String eventType) {
        return switch (eventType) {
            case "DeviceRegistered", "DeviceOnline", "DeviceOffline", "DeviceStatusChanged" ->
                "device-status-events";
            case "TelemetryReceived" -> "telemetry-events";
            case "DeviceCommandExecuted" -> "command-events";
            case "OTATaskCreated", "OTATaskCompleted" -> "ota-events";
            default -> "device-domain-events";
        };
    }

    /**
     * 从事件中提取租户ID作为消息键
     */
    private String extractTenantId(DomainEvent event) {
        // 通过反射或接口方法获取租户ID
        // 这里简化处理，实际应根据事件类型实现
        return event.eventType();
    }
}
