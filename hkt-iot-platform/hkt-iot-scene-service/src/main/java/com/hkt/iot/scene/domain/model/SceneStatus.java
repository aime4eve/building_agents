package com.hkt.iot.scene.domain.model;

/**
 * 场景状态枚举
 */
public enum SceneStatus {
    /**
     * 草稿 - 配置中，不可执行
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
     * 归档 - 已废弃，不可恢复
     */
    ARCHIVED
}
