package com.hkt.iot.device.application.event.kafka.consumer;

import com.alibaba.fastjson2.JSON;
import com.hkt.iot.device.application.event.kafka.topic.KafkaTopics;
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
 * 设备遥测数据消费者
 * 消费设备上报的遥测数据（温度、湿度等传感器数据）
 *
 * @author HKT IoT Team
 */
@Slf4j
@Component
public class DeviceTelemetryConsumer {

    /**
     * 消费设备遥测数据
     *
     * @param records 消息记录列表
     * @param ack     确认对象
     */
    @KafkaListener(
            topics = KafkaTopics.DEVICE_TELEMETRY_TOPIC,
            containerFactory = "telemetryListenerContainerFactory",
            groupId = "device-telemetry-consumer-group"
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
            log.debug("收到遥测数据消息, topic={}, partition={}, count={}", topic, partition, records.size());

            for (String record : records) {
                try {
                    DeviceTelemetryMessage message = JSON.parseObject(record, DeviceTelemetryMessage.class);
                    processDeviceTelemetry(message);
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
            log.info("遥测数据处理完成, success={}, fail={}, elapsed={}ms", successCount, failCount, elapsed);

        } catch (Exception e) {
            log.error("批量处理遥测数据失败, topic={}, partition={}", topic, partition, e);
        }
    }

    /**
     * 处理设备遥测数据
     */
    private void processDeviceTelemetry(DeviceTelemetryMessage message) {
        log.debug("处理遥测数据: deviceId={}, timestamp={}", message.getDeviceId(), message.getTimestamp());

        // TODO: 处理遥测数据
        // 1. 验证设备是否存在
        // 2. 验证设备状态是否在线
        // 3. 解析并存储遥测数据到时序数据库
        // 4. 更新设备最后通信时间
        // 5. 检查是否触发规则引擎
        // 6. 更新Redis缓存（最新遥测数据）
    }

    /**
     * 设备遥测数据消息
     */
    public static class DeviceTelemetryMessage {
        private String msgId;
        private String deviceId;
        private String deviceType;
        private Instant timestamp;
        private TelemetryData data;
        private Metadata metadata;

        // Getters and Setters
        public String getMsgId() { return msgId; }
        public void setMsgId(String msgId) { this.msgId = msgId; }
        public String getDeviceId() { return deviceId; }
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
        public String getDeviceType() { return deviceType; }
        public void setDeviceType(String deviceType) { this.deviceType = deviceType; }
        public Instant getTimestamp() { return timestamp; }
        public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
        public TelemetryData getData() { return data; }
        public void setData(TelemetryData data) { this.data = data; }
        public Metadata getMetadata() { return metadata; }
        public void setMetadata(Metadata metadata) { this.metadata = metadata; }
    }

    /**
     * 遥测数据
     */
    public static class TelemetryData {
        // 根据不同设备类型，数据字段不同
        // 例如：温度传感器会有temperature字段
        // 水表会有totalVolume、flowRate字段
        private Object data;

        public Object getData() { return data; }
        public void setData(Object data) { this.data = data; }
    }

    /**
     * 元数据
     */
    public static class Metadata {
        private Integer battery;
        private Integer rssi;

        public Integer getBattery() { return battery; }
        public void setBattery(Integer battery) { this.battery = battery; }
        public Integer getRssi() { return rssi; }
        public void setRssi(Integer rssi) { this.rssi = rssi; }
    }
}
