package com.hkt.iot.smartapps.smartlivestock.domain.model;

/**
 * 电子围栏类型枚举
 */
public enum GeofenceType {

    /**
     * 放牧区 - 允许牲畜活动的区域
     */
    GRAZING("放牧区"),

    /**
     * 限制区 - 禁止牲畜进入的区域
     */
    RESTRICTED("限制区"),

    /**
     * 隔离区 - 隔离病畜的区域
     */
    QUARANTINE("隔离区"),

    /**
     * 保护区 - 特殊保护的区域
     */
    PROTECTED("保护区");

    private final String displayName;

    GeofenceType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
