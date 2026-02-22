package com.hkt.iot.rule.infrastructure.messaging.kafka.topic;

/**
 * Kafka Topic常量定义
 * 规则引擎相关的Kafka主题
 *
 * @author HKT IoT Team
 */
public class KafkaTopics {

    /**
     * 设备遥测数据主题
     */
    public static final String DEVICE_TELEMETRY_TOPIC = "device-telemetry";

    /**
     * 设备状态变化主题
     */
    public static final String DEVICE_STATUS_TOPIC = "device-status";

    /**
     * 规则触发事件主题
     */
    public static final String RULE_TRIGGERED_TOPIC = "rule-triggered";

    /**
     * 规则执行失败事件主题
     */
    public static final String RULE_EXECUTION_FAILED_TOPIC = "rule-execution-failed";

    private KafkaTopics() {
        // 防止实例化
    }
}
