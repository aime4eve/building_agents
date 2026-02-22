package com.hkt.iot.rule.infrastructure.messaging.kafka.consumer;

import com.alibaba.fastjson2.JSON;
import com.hkt.iot.rule.domain.event.DeviceStatusChangedEvent;
import com.hkt.iot.rule.domain.service.RuleEventProcessingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * 设备状态变化消费者（规则引擎）
 * 消费设备状态变化事件并触发匹配的规则
 *
 * @author HKT IoT Team
 */
@Slf4j
@Component
public class DeviceStatusRuleConsumer {

    private final RuleEventProcessingService eventProcessingService;

    public DeviceStatusRuleConsumer(RuleEventProcessingService eventProcessingService) {
        this.eventProcessingService = eventProcessingService;
    }

    /**
     * 消费设备状态变化消息
     *
     * @param record 消息记录
     * @param ack    确认对象
     */
    @KafkaListener(
            topics = "${kafka.topic.device-status:device-status}",
            containerFactory = "deviceStatusListenerFactory",
            groupId = "rule-engine-status-group"
    )
    public void consumeDeviceStatusChanged(
            @Payload String record,
            Acknowledgment ack,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition) {

        try {
            log.debug("收到设备状态变化消息, topic={}, partition={}", topic, partition);

            DeviceStatusMessage message = JSON.parseObject(record, DeviceStatusMessage.class);
            processStatusChangedMessage(message);

            // 手动提交偏移量
            if (ack != null) {
                ack.acknowledge();
            }

        } catch (Exception e) {
            log.error("处理设备状态变化消息失败, topic={}, partition={}, message={}",
                    topic, partition, record, e);
        }
    }

    /**
     * 处理设备状态变化消息
     */
    private void processStatusChangedMessage(DeviceStatusMessage message) {
        log.debug("处理设备状态变化: deviceId={}, oldStatus={}, newStatus={}",
                message.getDeviceId(), message.getOldStatus(), message.getNewStatus());

        // 构建状态变化事件
        DeviceStatusChangedEvent event = new DeviceStatusChangedEvent(
                message.getDeviceId(),
                message.getDeviceSn(),
                message.getTenantId(),
                message.getOldStatus(),
                message.getNewStatus(),
                message.getChangedAt()
        );

        // 处理事件并触发规则
        eventProcessingService.processDeviceStatusChangedEvent(event);
    }

    /**
     * 设备状态变化消息
     */
    public static class DeviceStatusMessage {
        private String msgId;
        private Long deviceId;
        private String deviceSn;
        private Long tenantId;
        private String oldStatus;
        private String newStatus;
        private Instant changedAt;

        // Getters and Setters
        public String getMsgId() { return msgId; }
        public void setMsgId(String msgId) { this.msgId = msgId; }
        public Long getDeviceId() { return deviceId; }
        public void setDeviceId(Long deviceId) { this.deviceId = deviceId; }
        public String getDeviceSn() { return deviceSn; }
        public void setDeviceSn(String deviceSn) { this.deviceSn = deviceSn; }
        public Long getTenantId() { return tenantId; }
        public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
        public String getOldStatus() { return oldStatus; }
        public void setOldStatus(String oldStatus) { this.oldStatus = oldStatus; }
        public String getNewStatus() { return newStatus; }
        public void setNewStatus(String newStatus) { this.newStatus = newStatus; }
        public Instant getChangedAt() { return changedAt; }
        public void setChangedAt(Instant changedAt) { this.changedAt = changedAt; }
    }
}
