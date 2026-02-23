package com.hkt.iot.workflow.domain.model.valueobject;

/**
 * 流程节点类型枚举
 *
 * @author HKT IoT Team
 */
public enum FlowNodeType {
    /**
     * 开始节点
     */
    START,
    /**
     * 结束节点
     */
    END,
    /**
     * 审批节点
     */
    APPROVAL,
    /**
     * 服务调用节点
     */
    SERVICE,
    /**
     * 排他网关节点
     */
    GATEWAY_EXCLUSIVE,
    /**
     * 并行网关节点
     */
    GATEWAY_PARALLEL,
    /**
     * 通知节点
     */
    NOTIFICATION
}
