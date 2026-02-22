package com.hkt.iot.order.domain.model;

/**
 * 账单状态枚举
 *
 * @author HKT IoT Team
 */
public enum BillStatus {

    DRAFT("草稿"),

    PENDING("待确认"),

    CONFIRMED("已确认"),

    PAID("已支付"),

    OVERDUE("已逾期"),

    CANCELLED("已取消");

    private final String description;

    BillStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
