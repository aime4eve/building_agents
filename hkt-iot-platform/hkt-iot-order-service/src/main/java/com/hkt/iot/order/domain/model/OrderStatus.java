package com.hkt.iot.order.domain.model;

/**
 * 订单状态枚举
 *
 * @author HKT IoT Team
 */
public enum OrderStatus {

    PENDING("待支付"),

    PAID("已支付"),

    FULFILLED("已履约"),

    COMPLETED("已完成"),

    REFUNDED("已退款"),

    CANCELLED("已取消");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 判断是否可以支付
     */
    public boolean canPay() {
        return this == PENDING;
    }

    /**
     * 判断是否可以履约
     */
    public boolean canFulfill() {
        return this == PAID;
    }

    /**
     * 判断是否可以完成
     */
    public boolean canComplete() {
        return this == FULFILLED;
    }

    /**
     * 判断是否可以取消
     */
    public boolean canCancel() {
        return this == PENDING || this == PAID;
    }

    /**
     * 判断是否可以退款
     */
    public boolean canRefund() {
        return this == PAID || this == FULFILLED;
    }
}
