package com.hkt.iot.scene.domain.model;

/**
 * 定时计划状态枚举
 */
public enum ScheduleStatus {
    /**
     * 草稿 - 配置中
     */
    DRAFT,

    /**
     * 激活 - 可执行
     */
    ACTIVE,

    /**
     * 停用 - 暂停执行
     */
    INACTIVE,

    /**
     * 归档 - 已废弃
     */
    ARCHIVED
}
