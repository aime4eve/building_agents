package com.hkt.iot.rule.domain.model;

import java.util.Map;

/**
 * 规则类型配置
 * 定义不同类型规则的配置结构
 *
 * @author HKT IoT Team
 */
public interface RuleTypeConfig {

    /**
     * 获取规则类型
     */
    Rule.RuleType getType();

    /**
     * 获取类型名称
     */
    String getTypeName();

    /**
     * 验证配置
     *
     * @param config 规则配置
     * @return 验证结果
     */
    ValidationResult validateConfig(Map<String, Object> config);

    /**
     * 获取默认配置
     */
    Map<String, Object> getDefaultConfig();

    /**
     * 配置验证结果
     */
    record ValidationResult(boolean valid, String errorMessage) {
        public static ValidationResult valid() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult invalid(String errorMessage) {
            return new ValidationResult(false, errorMessage);
        }
    }

    /**
     * 告警规则配置
     */
    class AlarmRuleConfig implements RuleTypeConfig {

        public static final String THRESHOLD = "threshold";
        public static final String COMPARISON = "comparison";
        public static final String SEVERITY = "severity";
        public static final String DURATION = "duration";
        public static final String AGGREGATION = "aggregation";

        @Override
        public Rule.RuleType getType() {
            return Rule.RuleType.ALARM;
        }

        @Override
        public String getTypeName() {
            return "告警规则";
        }

        @Override
        public ValidationResult validateConfig(Map<String, Object> config) {
            if (config == null) {
                return ValidationResult.invalid("告警规则配置不能为空");
            }

            // 验证阈值
            if (!config.containsKey(THRESHOLD)) {
                return ValidationResult.invalid("告警规则必须配置阈值(threshold)");
            }

            // 验证比较操作符
            if (!config.containsKey(COMPARISON)) {
                return ValidationResult.invalid("告警规则必须配置比较操作符(comparison)");
            }
            String comparison = (String) config.get(COMPARISON);
            if (!isValidComparison(comparison)) {
                return ValidationResult.invalid("无效的比较操作符: " + comparison);
            }

            // 验证严重级别
            if (config.containsKey(SEVERITY)) {
                String severity = (String) config.get(SEVERITY);
                if (!isValidSeverity(severity)) {
                    return ValidationResult.invalid("无效的严重级别: " + severity);
                }
            }

            return ValidationResult.valid();
        }

        @Override
        public Map<String, Object> getDefaultConfig() {
            return Map.of(
                    THRESHOLD, 0,
                    COMPARISON, ">",
                    SEVERITY, "WARNING",
                    DURATION, 0,
                    AGGREGATION, "last"
            );
        }

        private boolean isValidComparison(String comparison) {
            return comparison != null && comparison.matches(">=|<=|>|<|==|!=");
        }

        private boolean isValidSeverity(String severity) {
            return "CRITICAL".equals(severity) ||
                   "WARNING".equals(severity) ||
                   "INFO".equals(severity);
        }
    }

    /**
     * 联动规则配置
     */
    class LinkageRuleConfig implements RuleTypeConfig {

        public static final String TRIGGER_CONDITION = "triggerCondition";
        public static final String TARGET_DEVICES = "targetDevices";
        public static final String ACTIONS = "actions";
        public static final String DELAY_MS = "delayMs";

        @Override
        public Rule.RuleType getType() {
            return Rule.RuleType.LINKAGE;
        }

        @Override
        public String getTypeName() {
            return "联动规则";
        }

        @Override
        public ValidationResult validateConfig(Map<String, Object> config) {
            if (config == null) {
                return ValidationResult.invalid("联动规则配置不能为空");
            }

            // 验证触发条件
            if (!config.containsKey(TRIGGER_CONDITION)) {
                return ValidationResult.invalid("联动规则必须配置触发条件(triggerCondition)");
            }

            // 验证目标设备
            if (!config.containsKey(TARGET_DEVICES)) {
                return ValidationResult.invalid("联动规则必须配置目标设备(targetDevices)");
            }

            // 验证动作
            if (!config.containsKey(ACTIONS)) {
                return ValidationResult.invalid("联动规则必须配置执行动作(actions)");
            }

            return ValidationResult.valid();
        }

        @Override
        public Map<String, Object> getDefaultConfig() {
            return Map.of(
                    TRIGGER_CONDITION, "",
                    TARGET_DEVICES, new java.util.ArrayList<Long>(),
                    ACTIONS, new java.util.ArrayList<Map<String, Object>>(),
                    DELAY_MS, 0
            );
        }
    }

    /**
     * 计费规则配置
     */
    class BillingRuleConfig implements RuleTypeConfig {

        public static final String RATE = "rate";
        public static final String UNIT = "unit";
        public static final String TIERED_RATES = "tieredRates";
        public static final String FIXED_FEE = "fixedFee";
        public static final String BILLING_CYCLE = "billingCycle";

        @Override
        public Rule.RuleType getType() {
            return Rule.RuleType.BILLING;
        }

        @Override
        public String getTypeName() {
            return "计费规则";
        }

        @Override
        public ValidationResult validateConfig(Map<String, Object> config) {
            if (config == null) {
                return ValidationResult.invalid("计费规则配置不能为空");
            }

            // 验证费率
            if (!config.containsKey(RATE)) {
                return ValidationResult.invalid("计费规则必须配置费率(rate)");
            }

            // 验证单位
            if (!config.containsKey(UNIT)) {
                return ValidationResult.invalid("计费规则必须配置单位(unit)");
            }

            // 验证计费周期
            if (!config.containsKey(BILLING_CYCLE)) {
                return ValidationResult.invalid("计费规则必须配置计费周期(billingCycle)");
            }

            return ValidationResult.valid();
        }

        @Override
        public Map<String, Object> getDefaultConfig() {
            return Map.of(
                    RATE, 0.0,
                    UNIT, "kWh",
                    TIERED_RATES, new java.util.ArrayList<Map<String, Object>>(),
                    FIXED_FEE, 0.0,
                    BILLING_CYCLE, "MONTHLY"
            );
        }
    }

    /**
     * 控制规则配置
     */
    class ControlRuleConfig implements RuleTypeConfig {

        public static final String CONTROL_TYPE = "controlType";
        public static final String TARGET_DEVICE = "targetDevice";
        public static final String CONTROL_VALUE = "controlValue";
        public static final String DURATION = "duration";

        @Override
        public Rule.RuleType getType() {
            return Rule.RuleType.CONTROL;
        }

        @Override
        public String getTypeName() {
            return "控制规则";
        }

        @Override
        public ValidationResult validateConfig(Map<String, Object> config) {
            if (config == null) {
                return ValidationResult.invalid("控制规则配置不能为空");
            }

            // 验证控制类型
            if (!config.containsKey(CONTROL_TYPE)) {
                return ValidationResult.invalid("控制规则必须配置控制类型(controlType)");
            }

            // 验证目标设备
            if (!config.containsKey(TARGET_DEVICE)) {
                return ValidationResult.invalid("控制规则必须配置目标设备(targetDevice)");
            }

            // 验证控制值
            if (!config.containsKey(CONTROL_VALUE)) {
                return ValidationResult.invalid("控制规则必须配置控制值(controlValue)");
            }

            return ValidationResult.valid();
        }

        @Override
        public Map<String, Object> getDefaultConfig() {
            return Map.of(
                    CONTROL_TYPE, "SWITCH",
                    TARGET_DEVICE, null,
                    CONTROL_VALUE, true,
                    DURATION, 0
            );
        }
    }
}
