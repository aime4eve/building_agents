package com.hkt.iot.workflow.domain.model.valueobject;

/**
 * SLA 状态枚举
 *
 * @author HKT IoT Team
 */
public enum SLAStatus {
    /**
     * 待处理
     */
    PENDING,
    /**
     * 符合 SLA
     */
    COMPLIANT,
    /**
     * 预警
     */
    WARNING,
    /**
     * 已超时
     */
    BREACHED
}
