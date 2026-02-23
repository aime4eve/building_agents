package com.hkt.iot.workflow.domain.model.valueobject;

/**
 * 流程状态枚举
 *
 * @author HKT IoT Team
 */
public enum ProcessInstanceState {
    /**
     * 已启动
     */
    STARTED,
    /**
     * 运行中
     */
    RUNNING,
    /**
     * 已挂起
     */
    SUSPENDED,
    /**
     * 已完成
     */
    COMPLETED,
    /**
     * 已失败
     */
    FAILED,
    /**
     * 已取消
     */
    CANCELLED
}
