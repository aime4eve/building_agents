package com.hkt.iot.order.domain.model;

/**
 * 订单类型枚举
 *
 * @author HKT IoT Team
 */
public enum OrderType {

    PRODUCT("产品订单"),

    SERVICE("服务订单"),

    ENERGY_BILL("能耗账单");

    private final String description;

    OrderType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
