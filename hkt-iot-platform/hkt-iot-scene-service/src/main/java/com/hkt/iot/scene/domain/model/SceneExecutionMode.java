package com.hkt.iot.scene.domain.model;

/**
 * 场景执行模式枚举
 */
public enum SceneExecutionMode {
    /**
     * 顺序执行 - 动作按顺序依次执行
     */
    SEQUENTIAL,

    /**
     * 并行执行 - 动作同时执行
     */
    PARALLEL
}
