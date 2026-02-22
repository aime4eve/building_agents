package com.hkt.iot.rule.domain.model;

/**
 * 规则类型枚举
 *
 * @author AI Engineer
 * @since 1.0.0
 */
public enum RuleType {
    /**
     * 告警规则 - 当条件满足时触发告警
     */
    ALARM("告警规则"),

    /**
     * 联动规则 - 当条件满足时执行设备控制等联动动作
     */
    LINKAGE("联动规则"),

    /**
     * 计费规则 - 基于使用量或其他条件计算费用
     */
    BILLING("计费规则"),

    /**
     * 控制规则 - 自动控制设备运行状态
     */
    CONTROL("控制规则");

    private final String description;

    RuleType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
