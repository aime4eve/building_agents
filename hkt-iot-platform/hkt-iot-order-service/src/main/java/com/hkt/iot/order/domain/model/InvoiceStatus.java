package com.hkt.iot.order.domain.model;

/**
 * 发票状态枚举
 *
 * @author HKT IoT Team
 */
public enum InvoiceStatus {

    PENDING("待开票"),

    ISSUED("已开票"),

    VOIDED("已作废");

    private final String description;

    InvoiceStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
