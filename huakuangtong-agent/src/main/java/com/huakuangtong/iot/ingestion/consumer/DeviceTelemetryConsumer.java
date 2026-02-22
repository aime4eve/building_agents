package com.huakuangtong.iot.ingestion.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huakuangtong.iot.ingestion.model.TelemetryMessage;
import com.huakuangtong.iot.ingestion.service.TelemetryProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 设备遥测数据消费者
 *
 * 从Kafka消费设备上报的遥测数据，并进行处理
 *
 * @author IoT Expert
 * @since 2026-02-20
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceTelemetryConsumer {

    private final ObjectMapper objectMapper;
    private final TelemetryProcessingService telemetryProcessingService;

    /**
     * 消费设备遥测数据
     *
     * Kafka Topic: device-telemetry
     * 分区策略: 按设备ID哈希，保证同一设备消息顺序
     * 消费者组: device-ingestion-service
     *
     * @param records Kafka消息记录
     * @param acknowledgment 手动确认
     */
    @KafkaListener(
        topics = "device-telemetry",
        groupId = "device-ingestion-service",
        concurrency = "10",
        properties = {
            "max.poll.records=100",
            "max.poll.interval.ms=300000",
            "enable.auto.commit=false"
        }
    )
    public void consumeTelemetry(List<ConsumerRecord<String, String>> records,
                                  Acknowledgment acknowledgment) {
        long startTime = System.currentTimeMillis();
        log.debug("Received {} telemetry messages", records.size());

        try {
            // 批量处理消息
            for (ConsumerRecord<String, String> record : records) {
                try {
                    // 解析消息
                    TelemetryMessage message = objectMapper.readValue(
                        record.value(),
                        TelemetryMessage.class
                    );

                    // 处理遥测数据
                    telemetryProcessingService.processTelemetry(message);

                    log.trace("Processed telemetry from device: {}, msgId: {}",
                        message.getDeviceId(), message.getMsgId());

                } catch (Exception e) {
                    log.error("Failed to process telemetry record, partition: {}, offset: {}, value: {}",
                        record.partition(), record.offset(), record.value(), e);

                    // 单条消息失败不影响批量提交
                    // 可选: 发送到死信队列
                }
            }

            // 手动提交偏移量
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }

            long duration = System.currentTimeMillis() - startTime;
            log.info("Processed {} telemetry messages in {} ms", records.size(), duration);

        } catch (Exception e) {
            log.error("Failed to consume telemetry batch", e);
            // 不提交偏移量，等待重试
        }
    }

    /**
     * 处理遥测数据 - 单条消息版本（用于低延迟场景）
     */
    @KafkaListener(
        topics = "device-telemetry",
        groupId = "device-ingestion-service-low-latency",
        concurrency = "5",
        properties = {
            "max.poll.records=10",
            "enable.auto.commit=false"
        }
    )
    public void consumeTelemetrySingle(ConsumerRecord<String, String> record,
                                        Acknowledgment acknowledgment) {
        try {
            TelemetryMessage message = objectMapper.readValue(
                record.value(),
                TelemetryMessage.class
            );

            telemetryProcessingService.processTelemetry(message);

            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }

            log.debug("Processed telemetry from device: {}, latency: {} ms",
                message.getDeviceId(),
                System.currentTimeMillis() - message.getTimestamp());

        } catch (Exception e) {
            log.error("Failed to process telemetry, deviceId: {}, value: {}",
                record.key(), record.value(), e);
        }
    }
}
