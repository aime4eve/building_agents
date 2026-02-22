package com.hkt.iot.rule.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 联动规则配置
 * 用于定义多设备协同控制的联动规则
 *
 * @author HKT IoT Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkageRule {

    private String linkageId;

    private String linkageName;

    private String description;

    private List<LinkageTrigger> triggers;

    private LogicalOperator triggerOperator;

    private List<LinkageAction> actions;

    private Integer executionDelay;

    private boolean enabled;

    /**
     * 逻辑运算符
     */
    public enum LogicalOperator {
        AND, OR
    }

    /**
     * 联动触发器
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LinkageTrigger {
        private TriggerType type;
        private Map<String, Object> config;

        public enum TriggerType {
            DEVICE_STATUS, TELEMETRY, SCHEDULE, EVENT
        }
    }

    /**
     * 联动动作
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LinkageAction {
        private ActionType type;
        private Long deviceId;
        private String commandCode;
        private Map<String, Object> params;
        private Integer delaySeconds;

        public enum ActionType {
            DEVICE_CONTROL, NOTIFICATION, SCENE, WEBHOOK
        }
    }

    /**
     * 评估触发条件
     */
    public boolean evaluateTriggers(Map<String, Object> context) {
        if (triggers == null || triggers.isEmpty()) {
            return false;
        }

        if (triggerOperator == LogicalOperator.AND) {
            return triggers.stream().allMatch(t -> evaluateTrigger(t, context));
        } else {
            return triggers.stream().anyMatch(t -> evaluateTrigger(t, context));
        }
    }

    private boolean evaluateTrigger(LinkageTrigger trigger, Map<String, Object> context) {
        switch (trigger.getType()) {
            case DEVICE_STATUS:
                return evaluateDeviceStatusTrigger(trigger, context);
            case TELEMETRY:
                return evaluateTelemetryTrigger(trigger, context);
            case SCHEDULE:
                return evaluateScheduleTrigger(trigger, context);
            case EVENT:
                return evaluateEventTrigger(trigger, context);
            default:
                return false;
        }
    }

    private boolean evaluateDeviceStatusTrigger(LinkageTrigger trigger, Map<String, Object> context) {
        Map<String, Object> config = trigger.getConfig();
        Object deviceIdObj = config.get("deviceId");
        if (deviceIdObj == null) return false;
        
        Long deviceId = ((Number) deviceIdObj).longValue();
        String statusField = (String) config.get("statusField");
        Object expectedValue = config.get("expectedValue");

        Object actualValue = context.get("device." + deviceId + "." + statusField);
        return expectedValue != null && expectedValue.equals(actualValue);
    }

    private boolean evaluateTelemetryTrigger(LinkageTrigger trigger, Map<String, Object> context) {
        Map<String, Object> config = trigger.getConfig();
        String field = (String) config.get("field");
        String operator = (String) config.get("operator");
        Object value = config.get("value");

        Object actualValue = context.get(field);
        if (actualValue == null || operator == null) {
            return false;
        }

        double actual = convertToDouble(actualValue);
        double expected = convertToDouble(value);

        return switch (operator) {
            case ">" -> actual > expected;
            case ">=" -> actual >= expected;
            case "<" -> actual < expected;
            case "<=" -> actual <= expected;
            case "==" -> actual == expected;
            case "!=" -> actual != expected;
            default -> false;
        };
    }

    private boolean evaluateScheduleTrigger(LinkageTrigger trigger, Map<String, Object> context) {
        return true;
    }

    private boolean evaluateEventTrigger(LinkageTrigger trigger, Map<String, Object> context) {
        Map<String, Object> config = trigger.getConfig();
        String eventType = (String) config.get("eventType");
        String actualEventType = (String) context.get("eventType");
        return eventType != null && eventType.equals(actualEventType);
    }

    private double convertToDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
