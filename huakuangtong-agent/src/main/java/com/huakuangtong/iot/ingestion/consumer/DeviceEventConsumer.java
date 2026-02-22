package com.huakuangtong.iot.ingestion.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huakuangtong.iot.ingestion.model.EventMessage;
import com.huakuangtong.iot.ingestion.service.EventProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 设备事件消费者
 *
 * 从Kafka消费设备上报的事件数据（如告警、故障等）
 *
 * @author IoT Expert
 * @since 2026-02-20
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceEventConsumer {

    private final ObjectMapper objectMapper;
    private final EventProcessingService eventProcessingService;

    /**
     * 消费设备事件数据
     *
     * Kafka Topic: device-event
     * 分区策略: 按租户ID哈希
     * 消费者组: device-ingestion-service
     *
     * @param records Kafka消息记录
     * @param acknowledgment 手动确认
     */
    @KafkaListener(
        topics = "device-event",
        groupId = "device-ingestion-service",
        concurrency = "5",
        properties = {
            "max.poll.records=50",
            "max.poll.interval.ms=300000",
            "enable.auto.commit=false"
        }
    )
    public void consumeEvent(List<ConsumerRecord<String, String>> records,
                             Acknowledgment acknowledgment) {
        long startTime = System.currentTimeMillis();
        log.debug("Received {} event messages", records.size());

        try {
            for (ConsumerRecord<String, String> record : records) {
                try {
                    EventMessage message = objectMapper.readValue(
                        record.value(),
                        EventMessage.class
                    );

                    // 处理事件数据
                    eventProcessingService.processEvent(message);

                    log.trace("Processed event from device: {}, eventType: {}, level: {}",
                        message.getDeviceId(),
                        message.getEventType(),
                        message.getEventLevel());

                } catch (Exception e) {
                    log.error("Failed to process event record, value: {}", record.value(), e);

                    // 告警事件失败需要特别处理，可能需要人工介入
                    // 发送到死信队列
                }
            }

            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }

            long duration = System.currentTimeMillis() - startTime;
            log.info("Processed {} event messages in {} ms", records.size(), duration);

        } catch (Exception e) {
            log.error("Failed to consume event batch", e);
        }
    }
}
