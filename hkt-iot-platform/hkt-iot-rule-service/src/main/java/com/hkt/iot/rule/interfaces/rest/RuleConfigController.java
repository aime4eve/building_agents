package com.hkt.iot.rule.interfaces.rest;

import com.hkt.iot.rule.domain.model.Rule;
import com.hkt.iot.rule.domain.model.RuleTypeConfig;
import com.hkt.iot.rule.domain.model.RuleTypeConfigFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 规则配置REST API控制器
 * 提供规则类型配置和模板管理功能
 *
 * @author HKT IoT Team
 */
@RestController
@RequestMapping("/api/v1/rules/config")
public class RuleConfigController {

    /**
     * 获取规则类型列表
     */
    @GetMapping("/types")
    public RuleTypeResponse getRuleTypes() {
        return new RuleTypeResponse(
                List.of("ALARM", "LINKAGE", "BILLING", "CONTROL"),
                Map.of(
                        "ALARM", "告警规则 - 设备数据触发告警",
                        "LINKAGE", "联动规则 - 多设备协同控制",
                        "BILLING", "计费规则 - 基于用量或时间计费",
                        "CONTROL", "控制规则 - 单设备控制"
                )
        );
    }

    /**
     * 获取指定规则类型的默认配置
     */
    @GetMapping("/types/{ruleType}/default")
    public Map<String, Object> getDefaultConfig(@PathVariable String ruleType) {
        Rule.RuleType type = Rule.RuleType.valueOf(ruleType);
        return RuleTypeConfigFactory.getDefaultConfig(type);
    }

    /**
     * 验证规则配置
     */
    @PostMapping("/types/{ruleType}/validate")
    public ConfigValidationResponse validateConfig(
            @PathVariable String ruleType,
            @RequestBody Map<String, Object> config) {
        Rule.RuleType type = Rule.RuleType.valueOf(ruleType);
        RuleTypeConfig.ValidationResult result =
                RuleTypeConfigFactory.validateConfig(type, config);

        return new ConfigValidationResponse(
                result.valid(),
                result.errorMessage(),
                null
        );
    }

    /**
     * 获取告警规则配置模板
     */
    @GetMapping("/templates/alarm")
    public Map<String, Object> getAlarmTemplate() {
        return Map.of(
                "typeName", RuleTypeConfigFactory.getTypeName(Rule.RuleType.ALARM),
                "description", "当设备数据满足条件时触发告警",
                "configSchema", Map.of(
                        "threshold", "告警阈值",
                        "comparison", "比较操作符 (>, >=, <, <=, ==, !=)",
                        "severity", "严重级别 (CRITICAL, WARNING, INFO)",
                        "duration", "持续时间(秒)，0表示立即触发",
                        "aggregation", "聚合方式 (last, avg, max, min, sum)"
                ),
                "example", Map.of(
                        "threshold", 30,
                        "comparison", ">",
                        "severity", "WARNING",
                        "duration", 60,
                        "aggregation", "avg"
                ),
                "expressionExample", "temperature > 30 and humidity < 50"
        );
    }

    /**
     * 获取联动规则配置模板
     */
    @GetMapping("/templates/linkage")
    public Map<String, Object> getLinkageTemplate() {
        return Map.of(
                "typeName", RuleTypeConfigFactory.getTypeName(Rule.RuleType.LINKAGE),
                "description", "当条件满足时，控制多个设备执行联动动作",
                "configSchema", Map.of(
                        "triggerCondition", "触发条件描述",
                        "targetDevices", "目标设备ID列表",
                        "actions", "动作列表",
                        "delayMs", "延迟执行时间(毫秒)"
                ),
                "example", Map.of(
                        "triggerCondition", "温度传感器温度超过30度",
                        "targetDevices", List.of(1001L, 1002L),
                        "actions", List.of(
                                Map.of("deviceId", 1001L, "action", "TURN_ON"),
                                Map.of("deviceId", 1002L, "action", "TURN_OFF")
                        ),
                        "delayMs", 0
                ),
                "expressionExample", "temperature > 30"
        );
    }

    /**
     * 获取计费规则配置模板
     */
    @GetMapping("/templates/billing")
    public Map<String, Object> getBillingTemplate() {
        return Map.of(
                "typeName", RuleTypeConfigFactory.getTypeName(Rule.RuleType.BILLING),
                "description", "根据设备用量或时间周期计算费用",
                "configSchema", Map.of(
                        "rate", "费率",
                        "unit", "计费单位",
                        "tieredRates", "阶梯费率配置",
                        "fixedFee", "固定费用",
                        "billingCycle", "计费周期 (DAILY, WEEKLY, MONTHLY)"
                ),
                "example", Map.of(
                        "rate", 0.5,
                        "unit", "kWh",
                        "tieredRates", List.of(),
                        "fixedFee", 10.0,
                        "billingCycle", "MONTHLY"
                ),
                "expressionExample", "energy_consumption > 0"
        );
    }

    /**
     * 获取控制规则配置模板
     */
    @GetMapping("/templates/control")
    public Map<String, Object> getControlTemplate() {
        return Map.of(
                "typeName", RuleTypeConfigFactory.getTypeName(Rule.RuleType.CONTROL),
                "description", "当条件满足时，控制指定设备执行动作",
                "configSchema", Map.of(
                        "controlType", "控制类型 (SWITCH, DIMMER, THERMOSTAT)",
                        "targetDevice", "目标设备ID",
                        "controlValue", "控制值",
                        "duration", "持续时间(秒)，0表示永久"
                ),
                "example", Map.of(
                        "controlType", "SWITCH",
                        "targetDevice", 2001L,
                        "controlValue", true,
                        "duration", 0
                ),
                "expressionExample", "motion_detected == true"
        );
    }

    // ==================== DTO类 ====================

    /**
     * 规则类型响应
     */
    public static class RuleTypeResponse {
        private final List<String> types;
        private final Map<String, String> descriptions;

        public RuleTypeResponse(List<String> types, Map<String, String> descriptions) {
            this.types = types;
            this.descriptions = descriptions;
        }

        public List<String> getTypes() { return types; }
        public Map<String, String> getDescriptions() { return descriptions; }
    }

    /**
     * 配置验证响应
     */
    public static class ConfigValidationResponse {
        private final boolean valid;
        private final String errorMessage;
        private final List<String> errors;

        public ConfigValidationResponse(boolean valid, String errorMessage, List<String> errors) {
            this.valid = valid;
            this.errorMessage = errorMessage;
            this.errors = errors;
        }

        public boolean isValid() { return valid; }
        public String getErrorMessage() { return errorMessage; }
        public List<String> getErrors() { return errors; }
    }
}
