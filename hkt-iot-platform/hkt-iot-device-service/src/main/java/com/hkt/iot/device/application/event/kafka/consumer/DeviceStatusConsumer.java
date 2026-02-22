package com.hkt.iot.device.application.event.kafka.consumer;

import com.alibaba.fastjson2.JSON;
import com.hkt.iot.device.application.event.kafka.topic.KafkaTopics;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * 设备状态消费者
 * 消费设备状态变更消息（上线、离线）
 *
 * @author HKT IoT Team
 */
@Slf4j
@Component
public class DeviceStatusConsumer {

    /**
     * 消费设备状态消息
     *
     * @param records 消息记录列表
     * @param ack     确认对象
     */
    @KafkaListener(
            topics = KafkaTopics.DEVICE_STATUS_TOPIC,
            containerFactory = "statusListenerContainerFactory",
            groupId = "device-status-consumer-group"
    )
    public void consumeDeviceStatus(
            @Payload List<String> records,
            Acknowledgment ack,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition) {

        try {
            log.debug("收到设备状态消息, topic={}, partition={}, count={}", topic, partition, records.size());

            for (String record : records) {
                DeviceStatusMessage message = JSON.parseObject(record, DeviceStatusMessage.class);
                processDeviceStatus(message);
            }

            // 手动提交偏移量
            if (ack != null) {
                ack.acknowledge();
            }
        } catch (Exception e) {
            log.error("处理设备状态消息失败, topic={}, partition={}", topic, partition, e);
            // 根据业务需求决定是否提交
        }
    }

    /**
     * 处理设备状态变更
     */
    private void processDeviceStatus(DeviceStatusMessage message) {
        log.info("处理设备状态: deviceId={}, status={}", message.getDeviceId(), message.getStatus());

        switch (message.getStatus()) {
            case "ONLINE" -> handleDeviceOnline(message);
            case "OFFLINE" -> handleDeviceOffline(message);
            case "FAULT" -> handleDeviceFault(message);
            default -> log.warn("未知的设备状态: {}", message.getStatus());
        }
    }

    /**
     * 处理设备上线
     */
    private void handleDeviceOnline(DeviceStatusMessage message) {
        log.info("设备上线: deviceId={}, tenantId={}, connectedAt={}",
                message.getDeviceId(), message.getTenantId(), message.getTimestamp());

        // TODO: 更新设备状态为在线
        // 1. 更新数据库
        // 2. 更新Redis缓存
        // 3. 发送设备上线事件
    }

    /**
     * 处理设备离线
     */
    private void handleDeviceOffline(DeviceStatusMessage message) {
        log.info("设备离线: deviceId={}, tenantId={}, reason={}",
                message.getDeviceId(), message.getTenantId(), message.getReason());

        // TODO: 更新设备状态为离线
        // 1. 更新数据库
        // 2. 更新Redis缓存
        // 3. 发送设备离线事件
        // 4. 如果是网关设备，需要标记子设备离线
    }

    /**
     * 处理设备故障
     */
    private void handleDeviceFault(DeviceStatusMessage message) {
        log.warn("设备故障: deviceId={}, tenantId={}",
                message.getDeviceId(), message.getTenantId());

        // TODO: 更新设备状态为故障
        // 1. 更新数据库
        // 2. 发送告警通知
    }

    /**
     * 设备状态消息
     */
    public static class DeviceStatusMessage {
        private String deviceId;
        private String deviceType;
        private String tenantId;
        private String status; // ONLINE, OFFLINE, FAULT
        private Instant timestamp;
        private String reason;
        private ConnectionInfo connectionInfo;

        // Getters and Setters
        public String getDeviceId() { return deviceId; }
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
        public String getDeviceType() { return deviceType; }
        public void setDeviceType(String deviceType) { this.deviceType = deviceType; }
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Instant getTimestamp() { return timestamp; }
        public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public ConnectionInfo getConnectionInfo() { return connectionInfo; }
        public void setConnectionInfo(ConnectionInfo connectionInfo) { this.connectionInfo = connectionInfo; }
    }

    /**
     * 连接信息
     */
    public static class ConnectionInfo {
        private String ipAddress;
        private String protocol;
        private String clientVersion;

        // Getters and Setters
        public String getIpAddress() { return ipAddress; }
        public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
        public String getProtocol() { return protocol; }
        public void setProtocol(String protocol) { this.protocol = protocol; }
        public String getClientVersion() { return clientVersion; }
        public void setClientVersion(String clientVersion) { this.clientVersion = clientVersion; }
    }
}
