package com.hkt.iot.device.infrastructure.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hkt.iot.device.domain.model.DeviceCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * MQTT命令发送器
 * 负责通过MQTT向设备发送控制命令
 *
 * @author HKT IoT Team
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MqttCommandSender {

    private final MessageChannel mqttOutboundChannel;
    private final ObjectMapper objectMapper;

    /**
     * 发送设备命令
     *
     * @param deviceSn 设备序列号
     * @param command 命令对象
     */
    public void sendCommand(String deviceSn, DeviceCommand command) {
        try {
            // 构建命令消息
            CommandMessage message = CommandMessage.builder()
                    .requestId(command.getRequestId())
                    .commandCode(command.getCommandCode())
                    .commandType(command.getCommandType().name())
                    .inputParams(command.getInputParams())
                    .timestamp(System.currentTimeMillis())
                    .build();

            // 序列化为JSON
            String payload = objectMapper.writeValueAsString(message);

            // 构建MQTT消息
            String topic = String.format("command/%s/down", deviceSn);
            Message<String> mqttMessage = MessageBuilder.withPayload(payload)
                    .setHeader(MqttHeaders.TOPIC, topic)
                    .setHeader(MqttHeaders.QOS, 1)
                    .setHeader(MqttHeaders.RETAINED, false)
                    .setHeader("deviceSn", deviceSn)
                    .setHeader("requestId", command.getRequestId())
                    .build();

            // 发送消息
            boolean sent = mqttOutboundChannel.send(mqttMessage, 5000);

            if (sent) {
                log.debug("MQTT命令发送成功: topic={}, requestId={}", topic, command.getRequestId());
            } else {
                throw new RuntimeException("MQTT消息发送超时");
            }

        } catch (Exception e) {
            log.error("MQTT命令发送失败: deviceSn={}, requestId={}, error={}",
                    deviceSn, command.getRequestId(), e.getMessage(), e);
            throw new RuntimeException("命令发送失败", e);
        }
    }

    /**
     * 广播命令到多个设备
     *
     * @param deviceSns 设备序列号列表
     * @param command   命令对象
     */
    public void broadcastCommand(java.util.List<String> deviceSns, DeviceCommand command) {
        log.info("广播命令到设备: count={}, commandCode={}",
                deviceSns.size(), command.getCommandCode());

        for (String deviceSn : deviceSns) {
            try {
                sendCommand(deviceSn, command);
            } catch (Exception e) {
                log.error("广播命令失败: deviceSn={}, error={}",
                        deviceSn, e.getMessage(), e);
            }
        }
    }

    /**
     * 命令消息
     */
    private static class CommandMessage {
        private final String requestId;
        private final String commandCode;
        private final String commandType;
        private final java.util.Map<String, Object> inputParams;
        private final Long timestamp;

        private CommandMessage(Builder builder) {
            this.requestId = builder.requestId;
            this.commandCode = builder.commandCode;
            this.commandType = builder.commandType;
            this.inputParams = builder.inputParams;
            this.timestamp = builder.timestamp;
        }

        public static Builder builder() {
            return new Builder();
        }

        public String getRequestId() { return requestId; }
        public String getCommandCode() { return commandCode; }
        public String getCommandType() { return commandType; }
        public java.util.Map<String, Object> getInputParams() { return inputParams; }
        public Long getTimestamp() { return timestamp; }

        public static class Builder {
            private String requestId;
            private String commandCode;
            private String commandType;
            private java.util.Map<String, Object> inputParams;
            private Long timestamp;

            public Builder requestId(String requestId) {
                this.requestId = requestId;
                return this;
            }

            public Builder commandCode(String commandCode) {
                this.commandCode = commandCode;
                return this;
            }

            public Builder commandType(String commandType) {
                this.commandType = commandType;
                return this;
            }

            public Builder inputParams(java.util.Map<String, Object> inputParams) {
                this.inputParams = inputParams;
                return this;
            }

            public Builder timestamp(Long timestamp) {
                this.timestamp = timestamp;
                return this;
            }

            public CommandMessage build() {
                return new CommandMessage(this);
            }
        }
    }
}
