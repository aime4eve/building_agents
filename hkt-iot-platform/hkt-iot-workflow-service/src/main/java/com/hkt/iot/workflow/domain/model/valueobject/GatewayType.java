package com.hkt.iot.workflow.domain.model.valueobject;

/**
 * 网关类型枚举
 *
 * @author HKT IoT Team
 */
public enum GatewayType {
    /**
     * 排他网关 - 只选择一条路径
     */
    EXCLUSIVE,
    /**
     * 并行网关 - 多条路径同时执行
     */
    PARALLEL,
    /**
     * 包容网关 - 可选择多条路径
     */
    INCLUSIVE
}
