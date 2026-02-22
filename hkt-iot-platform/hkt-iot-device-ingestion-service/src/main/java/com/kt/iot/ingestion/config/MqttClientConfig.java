package com.hkt.iot.ingestion.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hkt.iot.ingestion.model.EventMessage;
import com.hkt.iot.ingestion.model.StatusMessage;
import com.hkt.iot.ingestion.model.TelemetryMessage;
import com.hkt.iot.ingestion.service.MessageBridgeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

/**
 * MQTT客户端配置
 *
 * 使用Eclipse Paho MQTT客户端订阅EMQX消息
 *
 * @author IoT Expert
 * @since 2026-02-20
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class MqttClientConfig {

    private final ObjectMapper objectMapper;
    private final MessageBridgeService messageBridgeService;

    @Value("${mqtt.broker.url}")
    private String brokerUrl;

    @Value("${mqtt.broker.client-id}")
    private String clientId;

    @Value("${mqtt.broker.username}")
    private String username;

    @Value("${mqtt.broker.password}")
    private String password;

    @Value("${mqtt.broker.connection-timeout:10}")
    private int connectionTimeout;

    @Value("${mqtt.broker.keep-alive-interval:60}")
    private int keepAliveInterval;

    @Value("${mqtt.broker.automatic-reconnect:true}")
    private boolean automaticReconnect;

    @Value("${mqtt.topics.telemetry}")
    private String telemetryTopic;

    @Value("${mqtt.topics.event}")
    private String eventTopic;

    @Value("${mqtt.topics.status}")
    private String statusTopic;

    @Value("${mqtt.topics.heartbeat}")
    private String heartbeatTopic;

    @Value("${mqtt.qos:1}")
    private int qos;

    /**
     * MQTT客户端工厂
     */
    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        factory.setConnectionOptions(mqttConnectOptions());
        return factory;
    }

    /**
     * MQTT连接选项
     */
    @Bean
    public MqttConnectOptions mqttConnectOptions() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[]{brokerUrl});
        options.setUserName(username);
        options.setPassword(password.toCharArray());
        options.setConnectionTimeout(connectionTimeout);
        options.setKeepAliveInterval(keepAliveInterval);
        options.setAutomaticReconnect(automaticReconnect);
        options.setCleanSession(true);
        // 设置遗嘱消息
        options.setWill("device/lastwill", "disconnected".getBytes(), qos, false);
        return options;
    }

    /**
     * 遥测数据消息通道
     */
    @Bean
    public MessageChannel telemetryChannel() {
        return new DirectChannel();
    }

    /**
     * 设备事件消息通道
     */
    @Bean
    public MessageChannel eventChannel() {
        return new DirectChannel();
    }

    /**
     * 设备状态消息通道
     */
    @Bean
    public MessageChannel statusChannel() {
        return new DirectChannel();
    }

    /**
     * 心跳消息通道
     */
    @Bean
    public MessageChannel heartbeatChannel() {
        return new DirectChannel();
    }

    /**
     * MQTT遥测数据订阅适配器
     */
    @Bean
    public MqttPahoMessageDrivenChannelAdapter telemetryInbound(
            MqttPahoClientFactory mqttClientFactory) {
        MqttPahoMessageDrivenChannelAdapter adapter =
            new MqttPahoMessageDrivenChannelAdapter(clientId + "-telemetry", mqttClientFactory);
        adapter.setTopic(telemetryTopic);
        adapter.setQos(qos);
        adapter.setCompletionTimeout(30000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setPayloadType(String.class);
        adapter.setOutputChannel(telemetryChannel());
        return adapter;
    }

    /**
     * MQTT设备事件订阅适配器
     */
    @Bean
    public MqttPahoMessageDrivenChannelAdapter eventInbound(
            MqttPahoClientFactory mqttClientFactory) {
        MqttPahoMessageDrivenChannelAdapter adapter =
            new MqttPahoMessageDrivenChannelAdapter(clientId + "-event", mqttClientFactory);
        adapter.setTopic(eventTopic);
        adapter.setQos(qos);
        adapter.setCompletionTimeout(30000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setPayloadType(String.class);
        adapter.setOutputChannel(eventChannel());
        return adapter;
    }

    /**
     * MQTT设备状态订阅适配器
     */
    @Bean
    public MqttPahoMessageDrivenChannelAdapter statusInbound(
            MqttPahoClientFactory mqttClientFactory) {
        MqttPahoMessageDrivenChannelAdapter adapter =
            new MqttPahoMessageDrivenChannelAdapter(clientId + "-status", mqttClientFactory);
        adapter.setTopic(statusTopic);
        adapter.setQos(qos);
        adapter.setCompletionTimeout(30000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setPayloadType(String.class);
        adapter.setOutputChannel(statusChannel());
        return adapter;
    }

    /**
     * MQTT心跳订阅适配器
     */
    @Bean
    public MqttPahoMessageDrivenChannelAdapter heartbeatInbound(
            MqttPahoClientFactory mqttClientFactory) {
        MqttPahoMessageDrivenChannelAdapter adapter =
            new MqttPahoMessageDrivenChannelAdapter(clientId + "-heartbeat", mqttClientFactory);
        adapter.setTopic(heartbeatTopic);
        adapter.setQos(0); // QoS 0 for heartbeat
        adapter.setCompletionTimeout(30000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setPayloadType(String.class);
        adapter.setOutputChannel(heartbeatChannel());
        return adapter;
    }

    /**
     * 遥测数据消息处理器
     */
    @Bean
    @ServiceActivator(inputChannel = "telemetryChannel")
    public MessageHandler telemetryHandler() {
        return message -> {
            try {
                String payload = (String) message.getPayload();
                String topic = (String) message.getHeaders().get("mqtt_topic");
                log.debug("Received telemetry message, topic: {}, payload: {}", topic, payload);

                // 解析Topic获取设备信息
                TopicInfo topicInfo = parseTopic(topic);

                // 解析JSON消息
                TelemetryMessage telemetryMessage = objectMapper.readValue(payload, TelemetryMessage.class);
                telemetryMessage.setTenantId(topicInfo.getTenantId());
                telemetryMessage.setDeviceType(topicInfo.getDeviceType());

                // 发送到Kafka
                messageBridgeService.sendTelemetry(telemetryMessage, topic);

                log.debug("Telemetry processed and sent to Kafka, deviceId: {}", telemetryMessage.getDeviceId());

            } catch (Exception e) {
                log.error("Failed to handle telemetry message", e);
                throw new MessagingException("Failed to handle telemetry message", e);
            }
        };
    }

    /**
     * 设备事件消息处理器
     */
    @Bean
    @ServiceActivator(inputChannel = "eventChannel")
    public MessageHandler eventHandler() {
        return message -> {
            try {
                String payload = (String) message.getPayload();
                String topic = (String) message.getHeaders().get("mqtt_topic");
                log.debug("Received event message, topic: {}, payload: {}", topic, payload);

                TopicInfo topicInfo = parseTopic(topic);

                // 解析JSON消息
                EventMessage eventMessage = objectMapper.readValue(payload, EventMessage.class);
                eventMessage.setTenantId(topicInfo.getTenantId());
                eventMessage.setDeviceType(topicInfo.getDeviceType());

                // 发送到Kafka
                messageBridgeService.sendEvent(eventMessage, topic);

                log.debug("Event processed and sent to Kafka, deviceId: {}, eventType: {}",
                    eventMessage.getDeviceId(), eventMessage.getEventType());

            } catch (Exception e) {
                log.error("Failed to handle event message", e);
                throw new MessagingException("Failed to handle event message", e);
            }
        };
    }

    /**
     * 设备状态消息处理器
     */
    @Bean
    @ServiceActivator(inputChannel = "statusChannel")
    public MessageHandler statusHandler() {
        return message -> {
            try {
                String payload = (String) message.getPayload();
                String topic = (String) message.getHeaders().get("mqtt_topic");
                log.debug("Received status message, topic: {}, payload: {}", topic, payload);

                TopicInfo topicInfo = parseTopic(topic);

                // 解析JSON消息
                StatusMessage statusMessage = objectMapper.readValue(payload, StatusMessage.class);
                statusMessage.setTenantId(topicInfo.getTenantId());
                statusMessage.setDeviceType(topicInfo.getDeviceType());

                // 发送到Kafka
                messageBridgeService.sendStatus(statusMessage, topic);

                log.debug("Status processed and sent to Kafka, deviceId: {}, status: {}",
                    statusMessage.getDeviceId(), statusMessage.getStatus());

            } catch (Exception e) {
                log.error("Failed to handle status message", e);
                throw new MessagingException("Failed to handle status message", e);
            }
        };
    }

    /**
     * 心跳消息处理器
     */
    @Bean
    @ServiceActivator(inputChannel = "heartbeatChannel")
    public MessageHandler heartbeatHandler() {
        return new MessageHandler() {
            @Override
            public void handleMessage(Message<?> message) throws MessagingException {
                try {
                    String payload = (String) message.getPayload();
                    String topic = (String) message.getHeaders().get("mqtt_topic");
                    log.trace("Received heartbeat message, topic: {}, payload: {}", topic, payload);

                    TopicInfo topicInfo = parseTopic(topic);

                    // 更新设备心跳时间
                    log.debug("Heartbeat: deviceId={}", topicInfo.getDeviceId());

                } catch (Exception e) {
                    log.error("Failed to handle heartbeat message", e);
                }
            }
        };
    }

    /**
     * 解析MQTT Topic获取设备信息
     * Topic格式: device/{tenantId}/{deviceType}/{deviceId}/{messageType}
     */
    private TopicInfo parseTopic(String topic) {
        String[] parts = topic.split("/");
        if (parts.length >= 5) {
            return new TopicInfo(parts[1], parts[2], parts[3], parts[4]);
        }
        throw new IllegalArgumentException("Invalid topic format: " + topic);
    }

    /**
     * Topic信息
     */
    public static class TopicInfo {
        private final String tenantId;
        private final String deviceType;
        private final String deviceId;
        private final String messageType;

        public TopicInfo(String tenantId, String deviceType, String deviceId, String messageType) {
            this.tenantId = tenantId;
            this.deviceType = deviceType;
            this.deviceId = deviceId;
            this.messageType = messageType;
        }

        public String getTenantId() {
            return tenantId;
        }

        public String getDeviceType() {
            return deviceType;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public String getMessageType() {
            return messageType;
        }
    }
}
