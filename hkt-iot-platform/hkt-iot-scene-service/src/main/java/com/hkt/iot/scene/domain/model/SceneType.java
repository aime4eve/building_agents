package com.hkt.iot.scene.domain.model;

/**
 * 场景类型枚举
 */
public enum SceneType {
    /**
     * 手动场景 - 用户手动触发
     */
    MANUAL,

    /**
     * 自动场景 - 满足条件自动触发
     */
    AUTOMATIC,

    /**
     * 定时场景 - 按时间计划执行
     */
    SCHEDULED
}
