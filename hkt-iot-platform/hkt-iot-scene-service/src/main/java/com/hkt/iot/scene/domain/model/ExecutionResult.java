package com.hkt.iot.scene.domain.model;

/**
 * 执行结果枚举
 */
public enum ExecutionResult {
    /**
     * 成功 - 所有动作都执行成功
     */
    SUCCESS,

    /**
     * 部分成功 - 部分动作执行成功
     */
    PARTIAL_SUCCESS,

    /**
     * 失败 - 所有动作执行失败
     */
    FAILED
}
