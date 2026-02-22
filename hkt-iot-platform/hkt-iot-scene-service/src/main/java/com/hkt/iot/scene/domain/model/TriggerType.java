package com.hkt.iot.scene.domain.model;

/**
 * 触发类型枚举
 */
public enum TriggerType {
    /**
     * 设备事件 - 设备状态变化或事件触发
     */
    DEVICE_EVENT,

    /**
     * 时间条件 - 定时或周期性触发
     */
    TIME,

    /**
     * 手动触发 - 用户手动触发场景
     */
    MANUAL,

    /**
     * 条件表达式 - 满足复杂条件时触发
     */
    CONDITION
}
