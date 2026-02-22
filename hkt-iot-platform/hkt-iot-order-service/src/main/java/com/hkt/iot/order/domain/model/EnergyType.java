package com.hkt.iot.order.domain.model;

/**
 * 能耗类型枚举
 *
 * @author HKT IoT Team
 */
public enum EnergyType {

    ELECTRICITY("电"),

    WATER("水"),

    GAS("气");

    private final String description;

    EnergyType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
