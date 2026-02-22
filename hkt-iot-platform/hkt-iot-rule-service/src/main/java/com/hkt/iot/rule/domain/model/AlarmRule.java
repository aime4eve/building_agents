package com.hkt.iot.rule.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 告警规则配置
 * 用于定义设备告警的触发条件和告警信息
 *
 * @author HKT IoT Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlarmRule {

    private String alarmId;

    private String alarmName;

    private String alarmType;

    private AlarmLevel alarmLevel;

    private String alarmTitle;

    private String alarmMessage;

    private List<AlarmCondition> conditions;

    private AlarmAggregation aggregation;

    private Integer durationSeconds;

    private Integer silenceSeconds;

    private List<AlarmAction> actions;

    private boolean enabled;

    /**
     * 告警级别
     */
    public enum AlarmLevel {
        INFO(1, "信息"),
        WARNING(2, "警告"),
        ERROR(3, "错误"),
        CRITICAL(4, "严重");

        private final int level;
        private final String description;

        AlarmLevel(int level, String description) {
            this.level = level;
            this.description = description;
        }

        public int getLevel() {
            return level;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 告警条件
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlarmCondition {
        private String field;
        private String operator;
        private Object value;
        private String unit;

        public boolean evaluate(Map<String, Object> data) {
            Object fieldValue = data.get(field);
            if (fieldValue == null) {
                return false;
            }

            double actualValue = convertToDouble(fieldValue);
            double targetValue = convertToDouble(value);

            return switch (operator) {
                case ">" -> actualValue > targetValue;
                case ">=" -> actualValue >= targetValue;
                case "<" -> actualValue < targetValue;
                case "<=" -> actualValue <= targetValue;
                case "==" -> actualValue == targetValue;
                case "!=" -> actualValue != targetValue;
                default -> false;
            };
        }

        private double convertToDouble(Object value) {
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

    /**
     * 告警聚合策略
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlarmAggregation {
        private AggregationType type;
        private int windowSeconds;
        private int threshold;

        public enum AggregationType {
            COUNT, SUM, AVG, MAX, MIN
        }
    }

    /**
     * 告警动作
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlarmAction {
        private ActionType type;
        private Map<String, Object> config;

        public enum ActionType {
            NOTIFY, CONTROL, WEBHOOK, SCRIPT
        }
    }

    /**
     * 评估告警条件
     */
    public boolean evaluateConditions(Map<String, Object> data) {
        if (conditions == null || conditions.isEmpty()) {
            return false;
        }

        for (AlarmCondition condition : conditions) {
            if (!condition.evaluate(data)) {
                return false;
            }
        }

        return true;
    }
}
