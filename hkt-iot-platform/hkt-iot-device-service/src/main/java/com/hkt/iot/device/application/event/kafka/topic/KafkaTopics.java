package com.hkt.iot.device.application.event.kafka.topic;

/**
 * Kafka Topic常量定义
 * 定义设备接入层相关的所有Topic名称
 *
 * @author HKT IoT Team
 */
public class KafkaTopics {

    /**
     * 设备遥测数据Topic
     * 用于接收设备上报的遥测数据（温度、湿度、电压等）
     */
    public static final String DEVICE_TELEMETRY_TOPIC = "device-telemetry";

    /**
     * 设备事件Topic
     * 用于接收设备上报的事件（告警、故障等）
     */
    public static final String DEVICE_EVENT_TOPIC = "device-event";

    /**
     * 设备状态Topic
     * 用于接收设备状态变更（在线、离线）
     */
    public static final String DEVICE_STATUS_TOPIC = "device-status";

    /**
     * 设备命令Topic
     * 用于向设备下发控制命令
     */
    public static final String DEVICE_COMMAND_TOPIC = "device-command";

    /**
     * 设备命令响应Topic
     * 用于接收设备对命令的响应
     */
    public static final String DEVICE_COMMAND_RESPONSE_TOPIC = "device-command-response";

    /**
     * 设备心跳Topic
     * 用于接收设备心跳消息
     */
    public static final String DEVICE_HEARTBEAT_TOPIC = "device-heartbeat";

    private KafkaTopics() {
        throw new UnsupportedOperationException("Utility class");
    }
}
