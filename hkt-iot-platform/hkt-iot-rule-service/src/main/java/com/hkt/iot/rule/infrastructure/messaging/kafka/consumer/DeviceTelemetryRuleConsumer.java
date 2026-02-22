package com.hkt.iot.rule.infrastructure.messaging.kafka.consumer;

import com.alibaba.fastjson2.JSON;
import com.hkt.iot.rule.domain.event.TelemetryReceivedEvent;
import com.hkt.iot.rule.domain.service.RuleEventProcessingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * 设备遥测数据消费者（规则引擎）
 * 消费设备遥测数据并触发匹配的规则
 *
 * @author HKT IoT Team
 */
@Slf4j
@Component
public class DeviceTelemetryRuleConsumer {

    private final RuleEventProcessingService eventProcessingService;

    public DeviceTelemetryRuleConsumer(RuleEventProcessingService eventProcessingService) {
        this.eventProcessingService = eventProcessingService;
    }

    /**
     * 消费设备遥测数据
     *
     * @param records 消息记录列表
     * @param ack     确认对象
     */
    @KafkaListener(
            topics = "${kafka.topic.device-telemetry:device-telemetry}",
            containerFactory = "telemetryBatchListenerFactory",
            groupId = "rule-engine-telemetry-group"
    )
    public void consumeDeviceTelemetry(
            @Payload List<String> records,
            Acknowledgment ack,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition) {

        long startTime = System.currentTimeMillis();
        int successCount = 0;
        int failCount = 0;

        try {
            log.debug("收到遥测数据消息, topic={}, partition={}, count={}",
                    topic, partition, records.size());

            for (String record : records) {
                try {
                    TelemetryMessage message = JSON.parseObject(record, TelemetryMessage.class);
                    processTelemetryMessage(message);
                    successCount++;
                } catch (Exception e) {
                    log.error("处理单条遥测数据失败: {}", record, e);
                    failCount++;
                }
            }

            // 手动提交偏移量
            if (ack != null) {
                ack.acknowledge();
            }

            long elapsed = System.currentTimeMillis() - startTime;
            log.info("遥测数据处理完成, success={}, fail={}, elapsed={}ms",
                    successCount, failCount, elapsed);

        } catch (Exception e) {
            log.error("批量处理遥测数据失败, topic={}, partition={}", topic, partition, e);
        }
    }

    /**
     * 处理遥测数据消息
     */
    private void processTelemetryMessage(TelemetryMessage message) {
        log.debug("处理遥测数据: deviceId={}, timestamp={}",
                message.getDeviceId(), message.getTimestamp());

        // 构建遥测事件
        TelemetryReceivedEvent event = new TelemetryReceivedEvent(
                message.getDeviceId(),
                message.getDeviceSn(),
                message.getDeviceType(),
                message.getTenantId(),
                message.getSpaceId(),
                message.getData(),
                message.getTimestamp(),
                message.getMetadata()
        );

        // 处理事件并触发规则
        eventProcessingService.processTelemetryEvent(event);
    }

    /**
     * 遥测数据消息
     */
    public static class TelemetryMessage {
        private String msgId;
        private String deviceId;
        private String deviceSn;
        private String deviceType;
        private Long tenantId;
        private Long spaceId;
        private Instant timestamp;
        private java.util.Map<String, Object> data;
        private java.util.Map<String, Object> metadata;

        // Getters and Setters
        public String getMsgId() { return msgId; }
        public void setMsgId(String msgId) { this.msgId = msgId; }
        public String getDeviceId() { return deviceId; }
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
        public String getDeviceSn() { return deviceSn; }
        public void setDeviceSn(String deviceSn) { this.deviceSn = deviceSn; }
        public String getDeviceType() { return deviceType; }
        public void setDeviceType(String deviceType) { this.deviceType = deviceType; }
        public Long getTenantId() { return tenantId; }
        public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
        public Long getSpaceId() { return spaceId; }
        public void setSpaceId(Long spaceId) { this.spaceId = spaceId; }
        public Instant getTimestamp() { return timestamp; }
        public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
        public java.util.Map<String, Object> getData() { return data; }
        public void setData(java.util.Map<String, Object> data) { this.data = data; }
        public java.util.Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(java.util.Map<String, Object> metadata) { this.metadata = metadata; }
    }
}
