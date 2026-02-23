package com.hkt.iot.workflow.domain.model.valueobject;

/**
 * 任务状态枚举
 *
 * @author HKT IoT Team
 */
public enum TaskStatus {
    /**
     * 待处理
     */
    PENDING,
    /**
     * 处理中
     */
    IN_PROGRESS,
    /**
     * 已完成
     */
    COMPLETED,
    /**
     * 已取消
     */
    CANCELLED,
    /**
     * 已失败
     */
    FAILED
}
