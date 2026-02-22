package com.hkt.iot.rule.domain.model;

/**
 * 触发方式枚举
 *
 * @author AI Engineer
 * @since 1.0.0
 */
public enum TriggerType {
    /**
     * 实时触发 - 由设备事件实时触发
     */
    REALTIME("实时触发"),

    /**
     * 定时触发 - 按照cron表达式定时触发
     */
    SCHEDULED("定时触发"),

    /**
     * 手动触发 - 由用户手动触发
     */
    MANUAL("手动触发");

    private final String description;

    TriggerType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
