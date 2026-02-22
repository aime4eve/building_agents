package com.hkt.iot.scene.domain.model;

/**
 * 定时计划类型枚举
 */
public enum ScheduleType {
    /**
     * 每日执行
     */
    DAILY,

    /**
     * 每周执行
     */
    WEEKLY,

    /**
     * 每月执行
     */
    MONTHLY,

    /**
     * 自定义Cron表达式
     */
    CUSTOM
}
