package com.hkt.iot.device.infrastructure.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hkt.iot.device.application.service.TelemetryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * MQTT遥测数据处理器
 * 处理设备通过MQTT上报的遥测数据
 *
 * @author HKT IoT Team
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MqttTelemetryHandler {

    private final TelemetryService telemetryService;
    private final ObjectMapper objectMapper;

    /**
     * 处理设备遥测数据上报
     * 主题格式: telemetry/{deviceSn}/up
     */
    @ServiceActivator(inputChannel = "mqttInboundChannel")
    public void handleTelemetry(Message<byte[]> message) {
        try {
            String topic = (String) message.getHeaders().get("mqtt_topics");
            String payload = new String(message.getPayload());

            log.debug("收到MQTT遥测数据: topic={}, payload={}", topic, payload);

            // 解析主题获取设备信息
            String[] topicParts = topic.split("/");
            if (topicParts.length < 3) {
                log.warn("无效的MQTT主题: {}", topic);
                return;
            }

            String messageType = topicParts[0]; // telemetry
            String deviceSn = topicParts[1];    // 设备序列号

            if (!"telemetry".equals(messageType)) {
                log.warn("不支持的消息类型: {}", messageType);
                return;
            }

            // 解析遥测数据
            @SuppressWarnings("unchecked")
            Map<String, Object> telemetryData = objectMapper.readValue(payload, Map.class);

            // 提取租户ID和设备ID（从payload中获取或通过设备查询）
            Long tenantId = extractTenantId(telemetryData);
            Long deviceId = extractDeviceId(telemetryData);

            if (tenantId == null || deviceId == null) {
                log.warn("遥测数据缺少必要信息: deviceSn={}", deviceSn);
                return;
            }

            // 处理遥测数据
            telemetryService.receiveTelemetry(
                    tenantId,
                    deviceId,
                    deviceSn,
                    telemetryData,
                    LocalDateTime.now(),
                    null
            );

            log.debug("MQTT遥测数据处理成功: deviceSn={}", deviceSn);

        } catch (Exception e) {
            log.error("处理MQTT遥测数据失败: error={}", e.getMessage(), e);
        }
    }

    /**
     * 处理设备事件上报
     * 主题格式: event/{deviceSn}/up
     */
    public void handleEvent(Message<byte[]> message) {
        try {
            String topic = (String) message.getHeaders().get("mqtt_topics");
            String payload = new String(message.getPayload());

            log.debug("收到MQTT事件: topic={}, payload={}", topic, payload);

            // 解析事件数据
            @SuppressWarnings("unchecked")
            Map<String, Object> eventData = objectMapper.readValue(payload, Map.class);

            // 事件处理逻辑...
            log.debug("MQTT事件处理完成");

        } catch (Exception e) {
            log.error("处理MQTT事件失败: error={}", e.getMessage(), e);
        }
    }

    /**
     * 处理设备命令回执
     * 主题格式: command/{deviceSn}/receipt
     */
    public void handleCommandReceipt(Message<byte[]> message) {
        try {
            String topic = (String) message.getHeaders().get("mqtt_topics");
            String payload = new String(message.getPayload());

            log.debug("收到MQTT命令回执: topic={}, payload={}", topic, payload);

            // 解析回执数据
            @SuppressWarnings("unchecked")
            Map<String, Object> receiptData = objectMapper.readValue(payload, Map.class);

            // 回执处理逻辑...
            log.debug("MQTT命令回执处理完成");

        } catch (Exception e) {
            log.error("处理MQTT命令回执失败: error={}", e.getMessage(), e);
        }
    }

    /**
     * 从遥测数据中提取租户ID
     */
    private Long extractTenantId(Map<String, Object> data) {
        Object tenantId = data.get("tenantId");
        if (tenantId instanceof Number) {
            return ((Number) tenantId).longValue();
        }
        return null;
    }

    /**
     * 从遥测数据中提取设备ID
     */
    private Long extractDeviceId(Map<String, Object> data) {
        Object deviceId = data.get("deviceId");
        if (deviceId instanceof Number) {
            return ((Number) deviceId).longValue();
        }
        return null;
    }
}
