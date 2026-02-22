package com.huakuangtong.iot.ingestion.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huakuangtong.iot.ingestion.model.StatusMessage;
import com.huakuangtong.iot.ingestion.service.StatusSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 设备状态消费者
 *
 * 从Kafka消费设备状态变更消息
 * 用于同步设备在线/离线/故障等状态
 *
 * @author IoT Expert
 * @since 2026-02-20
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceStatusConsumer {

    private final ObjectMapper objectMapper;
    private final StatusSyncService statusSyncService;

    /**
     * 消费设备状态数据
     *
     * Kafka Topic: device-status
     * 分区策略: 按设备ID哈希
     * 消费者组: device-ingestion-service
     *
     * @param records Kafka消息记录
     * @param acknowledgment 手动确认
     */
    @KafkaListener(
        topics = "device-status",
        groupId = "device-ingestion-service",
        concurrency = "3",
        properties = {
            "max.poll.records=100",
            "max.poll.interval.ms=300000",
            "enable.auto.commit=false"
        }
    )
    public void consumeStatus(List<ConsumerRecord<String, String>> records,
                              Acknowledgment acknowledgment) {
        long startTime = System.currentTimeMillis();
        log.debug("Received {} status messages", records.size());

        try {
            for (ConsumerRecord<String, String> record : records) {
                try {
                    StatusMessage message = objectMapper.readValue(
                        record.value(),
                        StatusMessage.class
                    );

                    // 同步设备状态
                    statusSyncService.syncStatus(message);

                    log.trace("Synced status for device: {}, status: {}",
                        message.getDeviceId(),
                        message.getStatus());

                } catch (Exception e) {
                    log.error("Failed to process status record, value: {}", record.value(), e);
                }
            }

            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }

            long duration = System.currentTimeMillis() - startTime;
            log.info("Processed {} status messages in {} ms", records.size(), duration);

        } catch (Exception e) {
            log.error("Failed to consume status batch", e);
        }
    }
}
