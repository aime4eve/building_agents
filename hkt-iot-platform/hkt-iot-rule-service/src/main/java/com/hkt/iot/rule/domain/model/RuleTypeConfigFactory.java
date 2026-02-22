package com.hkt.iot.rule.domain.model;

import java.util.Map;

/**
 * 规则类型配置工厂
 * 根据规则类型创建对应的配置验证器
 *
 * @author HKT IoT Team
 */
public class RuleTypeConfigFactory {

    /**
     * 获取规则类型配置
     *
     * @param ruleType 规则类型
     * @return 规则类型配置
     */
    public static RuleTypeConfig getConfig(Rule.RuleType ruleType) {
        return switch (ruleType) {
            case ALARM -> new RuleTypeConfig.AlarmRuleConfig();
            case LINKAGE -> new RuleTypeConfig.LinkageRuleConfig();
            case BILLING -> new RuleTypeConfig.BillingRuleConfig();
            case CONTROL -> new RuleTypeConfig.ControlRuleConfig();
        };
    }

    /**
     * 验证规则配置
     *
     * @param ruleType 规则类型
     * @param config   规则配置
     * @return 验证结果
     */
    public static RuleTypeConfig.ValidationResult validateConfig(
            Rule.RuleType ruleType,
            Map<String, Object> config) {
        RuleTypeConfig typeConfig = getConfig(ruleType);
        return typeConfig.validateConfig(config);
    }

    /**
     * 获取默认配置
     *
     * @param ruleType 规则类型
     * @return 默认配置
     */
    public static Map<String, Object> getDefaultConfig(Rule.RuleType ruleType) {
        RuleTypeConfig typeConfig = getConfig(ruleType);
        return typeConfig.getDefaultConfig();
    }

    /**
     * 获取规则类型名称
     *
     * @param ruleType 规则类型
     * @return 类型名称
     */
    public static String getTypeName(Rule.RuleType ruleType) {
        RuleTypeConfig typeConfig = getConfig(ruleType);
        return typeConfig.getTypeName();
    }

    /**
     * 创建告警规则配置
     */
    public static Map<String, Object> createAlarmConfig(
            Object threshold,
            String comparison,
            String severity) {
        return Map.of(
                RuleTypeConfig.AlarmRuleConfig.THRESHOLD, threshold,
                RuleTypeConfig.AlarmRuleConfig.COMPARISON, comparison,
                RuleTypeConfig.AlarmRuleConfig.SEVERITY, severity,
                RuleTypeConfig.AlarmRuleConfig.DURATION, 0,
                RuleTypeConfig.AlarmRuleConfig.AGGREGATION, "last"
        );
    }

    /**
     * 创建联动规则配置
     */
    public static Map<String, Object> createLinkageConfig(
            String triggerCondition,
            java.util.List<Long> targetDevices,
            java.util.List<Map<String, Object>> actions) {
        return Map.of(
                RuleTypeConfig.LinkageRuleConfig.TRIGGER_CONDITION, triggerCondition,
                RuleTypeConfig.LinkageRuleConfig.TARGET_DEVICES, targetDevices,
                RuleTypeConfig.LinkageRuleConfig.ACTIONS, actions,
                RuleTypeConfig.LinkageRuleConfig.DELAY_MS, 0
        );
    }

    /**
     * 创建计费规则配置
     */
    public static Map<String, Object> createBillingConfig(
            Double rate,
            String unit,
            String billingCycle) {
        return Map.of(
                RuleTypeConfig.BillingRuleConfig.RATE, rate,
                RuleTypeConfig.BillingRuleConfig.UNIT, unit,
                RuleTypeConfig.BillingRuleConfig.BILLING_CYCLE, billingCycle,
                RuleTypeConfig.BillingRuleConfig.FIXED_FEE, 0.0,
                RuleTypeConfig.BillingRuleConfig.TIERED_RATES, new java.util.ArrayList<Map<String, Object>>()
        );
    }

    /**
     * 创建控制规则配置
     */
    public static Map<String, Object> createControlConfig(
            String controlType,
            Long targetDevice,
            Object controlValue) {
        return Map.of(
                RuleTypeConfig.ControlRuleConfig.CONTROL_TYPE, controlType,
                RuleTypeConfig.ControlRuleConfig.TARGET_DEVICE, targetDevice,
                RuleTypeConfig.ControlRuleConfig.CONTROL_VALUE, controlValue,
                RuleTypeConfig.ControlRuleConfig.DURATION, 0
        );
    }
}
